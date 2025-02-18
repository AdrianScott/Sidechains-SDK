package com.horizen.api.http

import akka.http.scaladsl.server.{MalformedRequestContentRejection, MethodRejection, Route}
import akka.http.scaladsl.model.{ContentTypes, HttpMethods, StatusCodes}
import com.fasterxml.jackson.databind.JsonNode
import com.horizen.api.http.SidechainTransactionErrorResponse.{ErrorByteTransactionParsing, ErrorNotFoundTransactionInput, GenericTransactionError}
import com.horizen.api.http.SidechainTransactionRestScheme.{ReqAllTransactions, ReqCreateRegularTransaction, ReqDecodeTransactionBytes, ReqSendCoinsToAddress, TransactionInput, TransactionOutput}
import com.horizen.box.{Box, BoxUnlocker, NoncedBox}
import com.horizen.proposition.PublicKey25519Proposition
import com.horizen.serialization.SerializationUtil
import com.horizen.transaction.{BoxTransaction, RegularTransaction, SidechainTransaction}
import com.horizen.utils.BytesUtils
import org.junit.Assert._

import scala.collection.JavaConverters._

class SidechainTransactionApiRouteTest extends SidechainApiRouteTest {

  override val basePath = "/transaction/"

  "The Api should to" should {

    "reject and reply with http error" in {
      Get(basePath) ~> sidechainTransactionApiRoute ~> check {
        rejection shouldBe MethodRejection(HttpMethods.POST)
      }
      Get(basePath) ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.MethodNotAllowed.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "allTransactions").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "allTransactions").withEntity("maybe_a_json") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "findById").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "findById").withEntity("maybe_a_json") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "decodeTransactionBytes").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "decodeTransactionBytes").withEntity("maybe_a_json") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "createRegularTransaction") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "createRegularTransaction").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "createRegularTransaction") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "createRegularTransactionSimplified") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "createRegularTransactionSimplified").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "createRegularTransactionSimplified") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "sendCoinsToAddress") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "sendCoinsToAddress").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "sendCoinsToAddress") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }

      Post(basePath + "sendTransaction").withEntity("maybe_a_json") ~> sidechainTransactionApiRoute ~> check {
        rejection.getClass.getCanonicalName.contains(MalformedRequestContentRejection.getClass.getCanonicalName.toString)
      }
      Post(basePath + "sendTransaction").withEntity("maybe_a_json") ~> Route.seal(sidechainTransactionApiRoute) ~> check {
        status.intValue() shouldBe StatusCodes.BadRequest.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }
    }

    "reply at /allTransactions" in {
      // parameter 'format' = true
      Post(basePath + "allTransactions")
        .withEntity(SerializationUtil.serialize(ReqAllTransactions(None))) ~> sidechainTransactionApiRoute ~> check {
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
        println(response)
        mapper.readTree(entityAs[String]).get("result") match {
          case result =>
            assertEquals(1, result.elements().asScala.length)
            assertTrue(result.get("transactions").isArray)
            assertEquals(2, result.get("transactions").elements().asScala.length)
          case _ => fail("Serialization failed for object SidechainApiResponseBody")
        }
      }
      // parameter 'format' = false
      Post(basePath + "allTransactions")
        .withEntity(SerializationUtil.serialize(ReqAllTransactions(Some(false)))) ~> sidechainTransactionApiRoute ~> check {
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
        mapper.readTree(entityAs[String]).get("result") match {
          case result =>
            assertEquals(1, result.elements().asScala.length)
            assertTrue(result.get("transactionIds").isArray)
            assertEquals(2, result.get("transactionIds").elements().asScala.length)
          case _ => fail("Serialization failed for object SidechainApiResponseBody")
        }
      }
    }

//    "reply at /findById" in {
//      // parameter 'format' = true
//      Post(basePath + "findById") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//      }
//    }

    "reply at /decodeTransactionBytes" in {
      Post(basePath + "decodeTransactionBytes")
        .withEntity(SerializationUtil.serialize(ReqDecodeTransactionBytes(
          BytesUtils.toHexString(transaction_1_bytes)))) ~> sidechainTransactionApiRoute ~> check {
        println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }
      Post(basePath + "decodeTransactionBytes")
        .withEntity(SerializationUtil.serialize(ReqDecodeTransactionBytes(
          BytesUtils.toHexString(transaction_1_bytes).replaceAll("a", "b")))) ~> sidechainTransactionApiRoute ~> check {
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
        assertsOnSidechainErrorResponseSchema(entityAs[String], ErrorByteTransactionParsing("", None).code)
      }
      Post(basePath + "decodeTransactionBytes")
        .withEntity(SerializationUtil.serialize(ReqDecodeTransactionBytes("AAABBBCCC"))) ~> sidechainTransactionApiRoute ~> check {
        status.intValue() shouldBe StatusCodes.InternalServerError.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }
    }

    "reply at /createRegularTransaction" in {
      // parameter 'format' = true
      val transactionInput: List[TransactionInput] = allBoxes.asScala.map(box => TransactionInput(BytesUtils.toHexString(box.id()))).toList
      val transactionOutput: List[TransactionOutput] = List(TransactionOutput(BytesUtils.toHexString(allBoxes.asScala.head.proposition().asInstanceOf[PublicKey25519Proposition].bytes), 30))
      Post(basePath + "createRegularTransaction")
        .withEntity(SerializationUtil.serialize(ReqCreateRegularTransaction(transactionInput,transactionOutput,Some(true)))) ~> sidechainTransactionApiRoute ~> check {
        //println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }
      // parameter 'format' = false
      Post(basePath + "createRegularTransaction")
        .withEntity(SerializationUtil.serialize(ReqCreateRegularTransaction(transactionInput,transactionOutput,Some(true)))) ~> sidechainTransactionApiRoute ~> check {
        println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }
      val transactionInput_2: List[TransactionInput] = transactionInput :+ TransactionInput("a_boxId")
      Post(basePath + "createRegularTransaction")
        .withEntity(SerializationUtil.serialize(ReqCreateRegularTransaction(transactionInput_2,transactionOutput,Some(true)))) ~> sidechainTransactionApiRoute ~> check {
        //println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
        assertsOnSidechainErrorResponseSchema(entityAs[String], ErrorNotFoundTransactionInput("", None).code)
      }
      Post(basePath + "createRegularTransaction")
        .withEntity(SerializationUtil.serialize(ReqCreateRegularTransaction(List(transactionInput_2.head),transactionOutput,None))) ~> sidechainTransactionApiRoute ~> check {
        println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
      }
    }
//
//    "reply at /createRegularTransactionSimplified" in {
//      // parameter 'format' = true
//      Post(basePath + "createRegularTransactionSimplified") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//      }
//      // parameter 'format' = false
//      Post(basePath + "createRegularTransactionSimplified") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//      }
//      Post(basePath + "createRegularTransactionSimplified") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
//      }
//    }
//
    "reply at /sendCoinsToAddress" in {
      // parameter 'format' = true
      val transactionOutput: List[TransactionOutput] = List(TransactionOutput(BytesUtils.toHexString(allBoxes.asScala.head.proposition().asInstanceOf[PublicKey25519Proposition].bytes), 30))
      Post(basePath + "sendCoinsToAddress")
        .withEntity(
          //"{\"outputs\": [{\"publicKey\": \"sadasdasfsdfsdfsdf\",\"value\": 12}],\"fee\": 30}"
          SerializationUtil.serialize(ReqSendCoinsToAddress(transactionOutput,None))
        ) ~> sidechainTransactionApiRoute ~> check {
        println(response)
        status.intValue() shouldBe StatusCodes.OK.intValue
        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
      }}
//      Post(basePath + "sendCoinsToAddress") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
//      }
//      Post(basePath + "sendCoinsToAddress") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
//      }
//    }
//
//    "reply at /sendTransaction" in {
//      // parameter 'format' = true
//      Post(basePath + "sendTransaction") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//      }
//      Post(basePath + "sendTransaction") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
//      }
//      Post(basePath + "sendTransaction") ~> sidechainTransactionApiRoute ~> check {
//        status.intValue() shouldBe StatusCodes.OK.intValue
//        responseEntity.getContentType() shouldEqual ContentTypes.`application/json`
//        println(response)
//        assertsOnSidechainErrorResponseSchema(entityAs[String], GenericTransactionError("", None).code)
//      }
//    }
  }
}