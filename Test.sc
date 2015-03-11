import java.util.Formatter.DateTime
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat

object Test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val currentTime = new java.util.Date(System.currentTimeMillis())
                                                  //> currentTime  : java.util.Date = Wed Mar 11 13:28:12 PDT 2015
	println(currentTime)                      //> Wed Mar 11 13:28:12 PDT 2015
  val myDate = new DateTime(currentTime)          //> myDate  : org.joda.time.DateTime = 2015-03-11T13:28:12.784-07:00
  println(myDate.toString("yyyy-MM-dd"))          //> 2015-03-11
 	val myApp = new Test()                    //> myApp  : Test = Test@152c7bf6
  println(myApp.calculatePower(5,4))              //> 625
}

class Test {
    def calculatePower(n:Int, p: Int): Int = {
    if (p == 0) return 1
    if (p == 1) return n
    
    return n * calculatePower(n, p-1)
  }
}