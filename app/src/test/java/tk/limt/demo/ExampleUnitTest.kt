package tk.limt.demo

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    interface A {
        fun print()
    }

    class A1(val a: Int = 1) : A {
        override fun print() {
            println("a1_$a")
        }
    }

    open class A2(open var a1: A1 = A1(2)) : A by a1 {

        fun pri() {
            a1.print()
        }
    }

    class A3 : A2() {
        override var a1 = A1(3)
        override fun print() {
//            super.print()
            println("a3")
        }
    }

    @Test
    fun addition_isCorrect() {
        println("start")
        val a1 = A1()
        val a2 = A2(a1)
        val a3 = A3()
//        a1.print()
//        a2.a1.print()
//        a2.print()
//        a2.pri()
//        a3.a1.print()
//        a3.print()
        a3.pri()
        println("end")
    }
}