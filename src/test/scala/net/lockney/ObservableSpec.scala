package net.lockney

import org.scalatest._
import org.scalatest.concurrent.{Eventually, AsyncAssertions}

import rx.Observable
import rx.schedulers.Schedulers
import rx.functions.Action1

import java.util.concurrent.TimeUnit

class ObservableSpec extends FlatSpec
                        with Matchers
                        with AsyncAssertions
                        with Eventually {

  import Helpers._

  behavior of "An Observable"

  //
  // *** Creating Observables
  //

  // ** create from a single object using Observable.just(...)
  it should "be able to return a basic object directly" in {

    val theObject = new Object(){ }

    val observable = Observable.just(theObject)

    assert(nextFromObservable(observable) === theObject)
  }


  // ** create from an Iterable instance (e.g., a standard Java collection)
  it should "be allow creation from a Collection" in {

    val list: java.util.List[String] = new java.util.ArrayList[String]()
    list.add("a")
    list.add("b")

    val observable = Observable.from(list)

    assert(nextFromObservable(observable) === "a")
    assert(nextFromObservable(observable) === "b")
  }

  it should "be able to return a value asynchronously" in {
    val w = new Waiter

    val value = "Something"

    val observable = Observable.just(value).observeOn(Schedulers.computation())

    observable.subscribe(new Action1[String]() {
      override def call(t1: String) {
        w {
            assert(t1 === value)
        }
        w.dismiss()
      }
    })

    w.await
  }

  it should "be able to return a series of intervals" in {
    val w = new Waiter

    var count = 0

    val observable = Observable.interval(10, TimeUnit.MILLISECONDS).take(5).observeOn(Schedulers.computation())

    observable.subscribe(new Action1[java.lang.Long]() {
      override def call(t1: java.lang.Long) {
        count += 1
        eventually {
                     if (t1 == 4) w.dismiss()
        }
      }
    })

    w.await
  }

  it should "be able to return a range of values" in {

    var range = (for (i <- 23 to 42) yield (i -> false)).toMap

    Observable.range(23, 20).subscribe(new Action1[java.lang.Integer](){
      override def call(t1: java.lang.Integer) {
        range = range.updated(t1, true)
      }
    })

    assert(range.forall(_._2 == true))
  }
}

object Helpers {
  // helper function to get the next item out of the observable -- not safe!
  def nextFromObservable[T](obs: Observable[T]): T = {
    obs.toBlockingObservable.toIterable.iterator().next()
  }
}
