package com.twitter.util.tunable

import org.scalatest.FunSuite

/**
 * A [[TunableMap]] that forwards all calls to `underlying`.
 */
trait TunableMapProxy extends TunableMap {

  protected def underlying: TunableMap

  def apply[T](key: TunableMap.Key[T]): Tunable[T] =
    underlying(key)

  def entries: Iterator[TunableMap.Entry[_]] = underlying.entries
}

class ServiceLoadedTunableTestClient1 extends ServiceLoadedTunableMap with TunableMapProxy {
  private val tunableMap = TunableMap.newMutable()

  tunableMap.put("tunableId1", "foo")
  tunableMap.put("tunableId2", 5)

  protected def underlying: TunableMap = tunableMap
  def id: String = "client1"
}

class ServiceLoadedTunableTestClient2 extends ServiceLoadedTunableMap with TunableMapProxy {
  protected def underlying: TunableMap = NullTunableMap
  def id: String = "client2"
}

class ServiceLoadedTunableTestClient2Dup extends ServiceLoadedTunableMap with TunableMapProxy {
  protected def underlying: TunableMap = NullTunableMap
  def id: String = "client2"
}

class ServiceLoadedTunableMapTest extends FunSuite {

  test("NullTunableMap returned when no matches") {
    val tunableMap = ServiceLoadedTunableMap("Non-existent-id")
    assert(tunableMap eq NullTunableMap)
  }

  test("TunableMap returned when there is one match for id") {
    val tunableMap = ServiceLoadedTunableMap("client1")

    assert(tunableMap.entries.size == 2)
    assert(tunableMap(TunableMap.Key[String]("tunableId1"))() == Some("foo"))
    assert(tunableMap(TunableMap.Key[Int]("tunableId2"))() == Some(5))
  }

  test("IllegalArgumentException thrown when there is more than one ServiceLoadedTunableMap " +
    "for a given serviceName/id") {

    intercept[IllegalStateException] {
      ServiceLoadedTunableMap("client2")
    }
  }
}
