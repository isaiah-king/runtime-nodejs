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

import common.TestHelpers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import common.WskTestHelpers
import common.Wsk
import common.WskProps
import java.io.File
import spray.json._
import common.TestUtils

@RunWith(classOf[JUnitRunner])
class CredentialsIBMNodeJsActionCloudantTests extends TestHelpers with WskTestHelpers {

  implicit val wskprops: WskProps = WskProps()
  var defaultKind = Some("nodejs:8")
  val wsk = new Wsk
  val datdir = System.getProperty("user.dir") + "/dat/"
  var creds = TestUtils.getVCAPcredentials("cloudantNoSQLDB")

  it should "Test whether or not cloudant database is reachable using cloudant npm module" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val file = Some(new File(datdir, "testCloudantAction.js").toString())

      assetHelper.withCleaner(wsk.action, "testCloudantAction") { (action, _) =>
        action.create(
          "testCloudantAction",
          file,
          main = Some("main"),
          kind = defaultKind,
          parameters = Map("username" -> JsString(creds.get("username")), "password" -> JsString(creds.get("password"))))
      }

      withActivation(wsk.activation, wsk.action.invoke("testCloudantAction")) { activation =>
        val response = activation.response
        response.result.get.fields.get("error") shouldBe empty
        response.result.get.fields.get("lastname") should be(Some(JsString("Queue")))
      }

  }

}
