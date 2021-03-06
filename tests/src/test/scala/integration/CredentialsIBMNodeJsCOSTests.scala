/*
 * Copyright 2017 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package integration

import common._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

import spray.json._
import org.scalatest.BeforeAndAfterAll
@RunWith(classOf[JUnitRunner])
class CredentialsIBMNodeJsCOSTests extends TestHelpers with WskTestHelpers with BeforeAndAfterAll {

  implicit val wskprops: WskProps = WskProps()
  var defaultKind = Some("nodejs:8")
  val wsk = new Wsk
  val datdir = System.getProperty("user.dir") + "/dat/"
  val creds = TestUtils.getVCAPcredentials("cloud-object-storage")
  val apikey = creds.get("apikey")
  var resource_instance_id = creds.get("resource_instance_id")
  val __bx_creds = JsObject(
    "cloud-object-storage" -> JsObject(
      "apikey" -> JsString(apikey),
      "resource_instance_id" -> JsString(resource_instance_id)))

  it should "Test connection to Cloud Object Storage COS on IBM Cloud" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val file = Some(new File(datdir, "cos/testcosread.js").toString())
      assetHelper.withCleaner(wsk.action, "testCOS") { (action, _) =>
        action.create(
          "testCOS",
          file,
          main = Some("main"),
          kind = defaultKind,
          parameters = Map("__bx_creds" -> __bx_creds))
      }
      withActivation(wsk.activation, wsk.action.invoke("testCOS")) { activation =>
        val response = activation.response
        response.result.get.fields.get("error") shouldBe empty
        response.result.get.fields.get("data") should be(
          Some(JsString("This is a test file for IBM-Functions integration testing.")))
      }
  }
}
