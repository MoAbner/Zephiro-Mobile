package com.meta.wearable.dat.externalsampleapps.cameraaccess.detection

enum class ControlledObjectClass(
    val modelLabel: String,
    val spokenName: String,
    val priority: Int,
) {
  CAR("car", "carro", 100),
  MOTORCYCLE("motorcycle", "moto", 95),
  BUS("bus", "onibus", 90),
  TRUCK("truck", "caminhao", 85),
  PERSON("person", "pessoa", 80),
  BICYCLE("bicycle", "bicicleta", 70),
  CHAIR("chair", "cadeira", 40),
  BOTTLE("bottle", "garrafa", 20);

  companion object {
    fun fromModelLabel(label: String): ControlledObjectClass? =
        entries.firstOrNull { it.modelLabel == label }
  }
}
