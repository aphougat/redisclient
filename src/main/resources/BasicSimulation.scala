/*
 * Copyright 2011-2018 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/json,application/xml;q=0.9,*/*;q=0.8") // 6
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  object Pincode {
    val feeder = csv("OfferCreate.csv").random // default is queue, so for this test, we use random to avoid feeder starvation
    val service =repeat(400, "i") { pause(2).feed(feeder)
      .exec(
        http("Put")
          .put("/offer")
          // Adds another header to the request
          .header("Keep-Alive", "150")
          // Overrides the Content-Type header
          .header("Content-Type", "application/json")
          //.body(StringBody("""{"pincode": "110001","item": [{"ussId": "124280GR0038322K10268","sellerId": "124280","isCOD": "N","price": 200.0,"fulfilmentType": ["TSHIP"],"transportMode": "AIR","deliveryModes": ["HD"],"isPrecious": "N","isFragile": "N"}]}"""))
          .body(StringBody("""{"id": "${OFFERID}","name": "${OFFERNAME}","offerType":"COMPLEX", "grade": "${GRADE}"}"""))
          .check(status is 200)
          .check(jsonPath("$..id").optional.saveAs("slaveId"))
      )/*.doIf("${slaveId.exists()}") {
      pause(5)
        .exec(session => {
        val response = session("BODY").as[String]
        println(s"Response body: \n$response")
        session
      })
    }*/
    }
  }

  val users = scenario("Users").exec(Pincode.service)
  setUp(
    users.inject(rampUsers(40) during (20 seconds))
  ).protocols(httpProtocol)
}