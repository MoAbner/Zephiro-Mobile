import cv2
import threading
import queue
import time
import subprocess
import pygame
from ultralytics import YOLO
import os

pygame.mixer.init()

AUDIOS = {
    "pessoa muito perto":       "audios/Pessoa_muito_perto.wav",
    "pessoa se aproximando":    "audios/Pessoa_se_aproximando.wav",
    "pessoa":                   "audios/Pessoa_distante.wav",
    "carro muito perto":        "audios/Carro_muito_perto.wav",
    "carro se aproximando":     "audios/Carro_se_aproximando.wav",
    "carro":                    "audios/Carro_distante.wav",
}

fila_fala = queue.Queue()

def worker_fala():
    while True:
        try:
            texto = fila_fala.get()
            if texto is None:
                break
            if texto in AUDIOS and os.path.exists(AUDIOS[texto]):
                som = pygame.mixer.Sound(AUDIOS[texto])
                som.play()
                while pygame.mixer.get_busy():
                    time.sleep(0.05)
            else:
                subprocess.run(
                    ['powershell', '-Command',
                     f'Add-Type -AssemblyName System.Speech; '
                     f'$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; '
                     f'$s.Rate = 2; $s.Speak("{texto}")'],
                    capture_output=True
                )
        except Exception as e:
            print(f"[AUDIO ERROR] {e}")
        finally:
            fila_fala.task_done()

threading.Thread(target=worker_fala, daemon=True).start()

# --- Configurações ---
TRADUCAO = {
    "person":        "pessoa",
    "car":           "carro",
    "truck":         "caminhão",
    "motorcycle":    "moto",
    "bicycle":       "bicicleta",
    "bus":           "ônibus",
    "dog":           "cachorro",
    "cat":           "gato",
    "chair":         "cadeira",
    "bottle":        "garrafa",
    "fire hydrant":  "hidrante",
}

COOLDOWN = 4.0
CONF     = 0.45

# --- Lógica de proximidade ---
def get_nivel(box, frame_area):
    x1, y1, x2, y2 = box.xyxy[0]
    ratio = float((x2 - x1) * (y2 - y1)) / frame_area
    if ratio > 0.25:
        return "muito perto"
    elif ratio > 0.08:
        return "perto"
    else:
        return "longe"

def montar_frase(nome, nivel):
    if nivel == "muito perto":
        return f"{nome} muito perto"
    elif nivel == "perto":
        return f"{nome} se aproximando"
    else:
        return nome

# --- Controle de cooldown ---
ultimo_alerta = {}

def alertar(classe, nivel):
    agora = time.time()
    if agora - ultimo_alerta.get(classe, 0) < COOLDOWN:
        return
    ultimo_alerta[classe] = agora

    frase = montar_frase(TRADUCAO.get(classe, classe), nivel)
    if fila_fala.empty():
        fila_fala.put(frase)
        print(f"[{nivel.upper()}] {frase}")

# --- Cores por proximidade ---
COR_NIVEL = {
    "muito perto": (0, 0, 255),
    "perto":       (0, 165, 255),
    "longe":       (0, 255, 0),
}

# --- Loop principal ---
model = YOLO("yolov8n.pt")  
cap   = cv2.VideoCapture(0)


# --- Thread de saída pelo console ---
sair = threading.Event()

def escutar_teclado():
    input("Iniciando... pressione Q para sair\n")
    sair.set()

threading.Thread(target=escutar_teclado, daemon=True).start()

while True:
    ret, frame = cap.read()
    if not ret:
        print("Erro ao abrir câmera")
        break

    frame_area = frame.shape[0] * frame.shape[1]
    results    = model(frame, conf=CONF, verbose=False)[0]

    for box in results.boxes:
        classe = model.names[int(box.cls)]
        nivel  = get_nivel(box, frame_area)
        cor    = COR_NIVEL[nivel]
        nome   = TRADUCAO.get(classe, classe)

        x1, y1, x2, y2 = map(int, box.xyxy[0])
        conf = float(box.conf[0])

        cv2.rectangle(frame, (x1, y1), (x2, y2), cor, 2)
        cv2.putText(frame, f"{nome} {conf:.0%} [{nivel}]",
                    (x1, y1 - 8), cv2.FONT_HERSHEY_SIMPLEX,
                    0.55, cor, 2)

        alertar(classe, nivel)

    cv2.imshow("Smart Glasses", frame)
    if cv2.waitKey(1) & 0xFF == ord('q') or sair.is_set():
        break

fila_fala.put(None)
cap.release()
cv2.destroyAllWindows()
print("Encerrado.")