package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MisPruebasTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  // Punto 1 - girar

  test("girar: de Este a la derecha queda Sur") {

    assert(girar(Este, Derecha) == Sur)

  }

  test("girar: de Este a la izquierda queda Norte") {

    assert(girar(Este, Izquierda) == Norte)

  }

  test("girar: de Sur a la derecha queda Oeste") {

    assert(girar(Sur, Derecha) == Oeste)

  }

  test("girar: de Oeste a la izquierda queda Sur") {

    assert(girar(Oeste, Izquierda) == Sur)

  }

  test("girar: tres giros a la izquierda desde el Norte dan Este") {

    assert(girar(girar(girar(Norte, Izquierda), Izquierda), Izquierda) == Este)

  }

  // Punto 2 - Ejecutar

  test("ejecutar: girar a la izquierda no mueve al robot") {

    assert(ejecutar(Estado(0, 0, Norte), Girar(Izquierda)) == Estado(0, 0, Oeste))

  }

  test("ejecutar: avanzar mirando al Este suma a x") {

    assert(ejecutar(Estado(1, 1, Este), Avanzar(3)) == Estado(4, 1, Este))

  }

  test("ejecutar: retroceder mirando al Oeste suma a x") {

    assert(ejecutar(Estado(2, 3, Oeste), Avanzar(-1)) == Estado(3, 3, Oeste))

  }

  test("ejecutar: repetir tres veces un avance de dos") {

    assert(ejecutar(Estado(0, 0, Norte), Repetir(3, Avanzar(2))) == Estado(0, 6, Norte))

  }

  test("ejecutar: una repeticion dentro de una secuencia") {

    val o = Secuencia(List(
      Avanzar(1),
      Repetir(2, Secuencia(List(Avanzar(1), Girar(Izquierda)))),
      Avanzar(5)
    ))
    assert(ejecutar(Estado(0, 0, Norte), o) == Estado(-1, -3, Sur))

  }

  // Punto 3 - cuantasBasicas y Profundidad

  test("cuantasBasicas: una secuencia vacia no tiene ordenes") {

    assert(cuantasBasicas(Secuencia(Nil)) == 0)

  }

  test("cuantasBasicas: una repeticion de tres ordenes tres veces") {

    val o = Repetir(3, Secuencia(List(Avanzar(1), Avanzar(2), Girar(Derecha))))
    assert(cuantasBasicas(o) == 9)

  }

  test("cuantasBasicas: repeticiones anidadas se multiplican") {

    assert(cuantasBasicas(Repetir(2, Repetir(3, Avanzar(1)))) == 6)

  }

  test("profundidad: un Repetir sobre una basica tiene 2") {

    assert(profundidad(Repetir(5, Avanzar(1))) == 2)

  }

  test("profundidad: dos Repetir anidados tienen 3") {

    assert(profundidad(Repetir(2, Repetir(3, Avanzar(1)))) == 3)

  }

  test("profundidad: dos repetir anidados dentro de una secuencia tienen 4") {

    assert(profundidad(Secuencia(List(Repetir(2, Repetir(2, Girar(Derecha)))))) == 4)

  }

  // Punto 4 - Componer, Rutina, AplicarA y Transformar

  test("componer: junta dos ordenes en una secuencia") {

    assert(componer(Girar(Izquierda), Avanzar(2)) == Secuencia(List(Girar(Izquierda), Avanzar(2))))

  }

  test("componer: ejecutar la compuesta avanza y luego gira") {

    assert(ejecutar(Estado(0, 0, Norte), componer(Avanzar(1), Girar(Derecha))) == Estado(0, 1, Este))

  }

  test("rutina: construye el repetir con la secuencia esperada") {

    assert(rutina(2)(Izquierda)(1) == Repetir(1, Secuencia(List(Avanzar(2), Girar(Izquierda)))))

  }

  test("rutina: dos vueltas de tres pasos girando a la derecha") {

    assert(ejecutar(Estado(0, 0, Norte), rutina(3)(Derecha)(2)) == Estado(3, 3, Sur))

  }

  test("aplicarA: girar a la derecha sobre dos estados") {

    val estados = List(Estado(1, 1, Este), Estado(0, 0, Oeste))
    assert(aplicarA(estados, Girar(Derecha)) == List(Estado(1, 1, Sur), Estado(0, 0, Norte)))

  }

  test("aplicarA: una orden que no hace nada deja los estados igual") {

    val estados = List(Estado(2, 2, Norte))
    assert(aplicarA(estados, Secuencia(Nil)) == estados)

  }

  test("transformar: cambia los giros y respeta los avances") {

    val cambiarGiro: Orden => Orden = {

      case Girar(_) => Girar(Derecha)
      case otra => otra

    }

    val o = Secuencia(List(Girar(Izquierda), Avanzar(2)))
    assert(transformar(o, cambiarGiro) == Secuencia(List(Girar(Derecha), Avanzar(2))))

  }

  test("transformar: entra dentro de un Repetir y conserva las veces") {

    val cambiarGiro: Orden => Orden = {

      case Girar(_) => Girar(Izquierda)
      case otra => otra

    }

    val o = Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha))))
    assert(transformar(o, cambiarGiro) == Repetir(3, Secuencia(List(Avanzar(1), Girar(Izquierda)))))

  }

  // Punto 5 - Simplificar

  test("simplificar: una basica no cambia") {

    assert(simplificar(Girar(Izquierda)) == Girar(Izquierda))

  }

  test("simplificar: quita varios Avanzar(0) y queda la orden que importa") {

    val o = Secuencia(List(Girar(Derecha), Avanzar(0), Avanzar(0)))
    assert(simplificar(o) == Girar(Derecha))

  }

  test("simplificar: junta tres avances seguidos en uno") {

    val o = Secuencia(List(Avanzar(1), Avanzar(2), Avanzar(3)))
    assert(simplificar(o) == Avanzar(6))

  }

  test("simplificar: una secuencia dentro de otra se simplifica primero") {

    val o = Secuencia(List(Secuencia(List(Avanzar(1), Avanzar(2))), Avanzar(4)))
    assert(simplificar(o) == Avanzar(7))

  }

  test("simplificar: simplifica por dentro de un Repetir") {

    val o = Repetir(3, Secuencia(List(Avanzar(1), Avanzar(1))))
    assert(simplificar(o) == Repetir(3, Avanzar(2)))

  }

  test("simplificar: repetir algo que no hace nada no hace nada") {

    assert(simplificar(Repetir(5, Avanzar(0))) == Secuencia(Nil))

  }

  test("simplificar: una orden con repetir termina en el mismo estado") {

    val o = Secuencia(List(
      Repetir(2, Secuencia(List(Avanzar(1), Avanzar(1), Girar(Izquierda)))),
      Avanzar(0),
      Avanzar(2)
    ))
    val e = Estado(0, 0, Este)
    assert(ejecutar(e, simplificar(o)) == ejecutar(e, o))

  }
  
}