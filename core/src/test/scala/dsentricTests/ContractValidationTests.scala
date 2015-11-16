package dsentricTests

import dsentric._
import dsentric.MaybePessimistic
import org.scalatest.{Matchers, FunSuite}

class ContractValidationTests extends FunSuite with Matchers with FailureMatchers {

  import J._

  object Empty extends Contract

  test("validation of contract type")  {

    Empty.$validate(JsBool(false)) should failWith(ValidationText.UNEXPECTED_TYPE)
    Empty.$validate(JsObject(Map.empty)) should be (Failures.empty)
    Empty.$validate(JsObject(Map("key" -> JsString("value")))) should be (Failures.empty)
  }


  object ExpectedField extends Contract {

    val expGT = \[Int](Validator.>(5))
//    val inDefault = \![String]("default", Validator.in("default", "one", "two"))
  }

  test("validation of expected field") {

    ExpectedField.$validate(JsString("fail")) should be (Failures(Path.empty -> ValidationText.UNEXPECTED_TYPE))
    ExpectedField.$validate(JsObject(Map.empty)) should be (Failures(Path("expGT") -> ValidationText.EXPECTED_VALUE))
    ExpectedField.$validate(JsObject(Map("expGT" -> JsBool(false)))) should be (Failures(Path("expGT") -> ValidationText.UNEXPECTED_TYPE))
    ExpectedField.$validate(JsObject(Map("expGT" -> JsNumber(7)))) should be (Failures.empty)
    ExpectedField.$validate(JsObject(Map("expGT" -> JsNumber(3)))) should failWith(Path("expGT"))
  }

  object MaybeField extends Contract {

    implicit def strictness = MaybePessimistic

    val mayNonEmpty = \?[String](Validator.nonEmptyOrWhiteSpace)

  }

  test("validation of optional field") {
    MaybeField.$validate(JsObject(Map.empty)) should be (Failures.empty)
    MaybeField.$validate(JsObject(Map("mayNonEmpty" -> JsBool(false)))) should be (Failures(Path("mayNonEmpty") -> ValidationText.UNEXPECTED_TYPE))
    MaybeField.$validate(JsObject(Map("mayNonEmpty" -> JsString("TEST")))) should be (Failures.empty)
    MaybeField.$validate(JsObject(Map("mayNonEmpty" -> JsString("")))) should failWith(Path("mayNonEmpty"))
  }
}