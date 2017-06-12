import com.sun.net.httpserver._;
import java.net._;
import java.io._;
import com.google.gson._;
import java.util.Base64;
import java.nio.ByteBuffer;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.Map;
import scala.collection.JavaConversions._;
import scala.collection.JavaConverters._;

object Tarea4 {
  def main(args: Array[String]): Unit = {
    val server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/ejercicio1", new handler_ejercicio1());
    server.createContext("/ejercicio2", new handler_ejercicio2());
    server.createContext("/ejercicio3", new handler_ejercicio3());
    server.createContext("/ejercicio4", new handler_ejercicio4());
    server.start();
    println("Server up!");
  }
}

class handler_ejercicio1() extends HttpHandler{
  override def handle(client: HttpExchange){
    try{
      if(client.getRequestMethod() == "POST"){
        val jsonReq = new JsonParser().parse(new String(scala.io.Source.fromInputStream(client.getRequestBody()).mkString)).getAsJsonObject();
        var mapsLink = "https://maps.googleapis.com/maps/api/directions/json?origin=ORIGIN&destination=DESTINATION&key=AIzaSyA7sQSQEOesLMKtCLmqISRpv7YHeWL67-c";
        try{
          mapsLink = mapsLink.replace("ORIGIN", jsonReq.get("origen").getAsString().replace(" ", "+"));
          mapsLink = mapsLink.replace("DESTINATION", jsonReq.get("destino").getAsString().replace(" ", "+"));
        }catch{
          case _: Throwable =>
          val response = "{\"error\": \"No se especifico origen\"}";
          client.getResponseHeaders().add("content-type", "json");
          client.sendResponseHeaders(400, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
          client.getResponseBody().write(response.getBytes("UTF-8"));
          client.getResponseBody().close();
        }
        val conn = (new URL(mapsLink).openConnection()).asInstanceOf[HttpURLConnection];
        conn.setRequestMethod("GET");
        val mapsResp = new String(scala.io.Source.fromInputStream(conn.getInputStream()).mkString);
        val routeOnMaps = new JsonParser().parse(mapsResp).getAsJsonObject().get("routes").getAsJsonArray().get(0).getAsJsonObject().get("legs").getAsJsonArray().get(0).getAsJsonObject().get("steps").getAsJsonArray();
        var ruta = Map[String, ArrayBuffer[JsonObject]]();
        ruta("ruta") = new ArrayBuffer[JsonObject]();
        var temp = new JsonObject();
        temp.add("lat", routeOnMaps.get(0).getAsJsonObject().get("start_location").getAsJsonObject().get("lat"));
        temp.add("lon", routeOnMaps.get(0).getAsJsonObject().get("start_location").getAsJsonObject().get("lng"));
        ruta("ruta") += temp;
        for(location <- routeOnMaps){
          temp = new JsonObject();
          temp.add("lat", location.getAsJsonObject().get("end_location").getAsJsonObject().get("lat"));
          temp.add("lon", location.getAsJsonObject().get("end_location").getAsJsonObject().get("lng"));
          ruta("ruta") += temp;
        }
        var response = new GsonBuilder().create().toJson(ruta("ruta").asJava);
        response = "{\"ruta\":"+response+"}";
        client.getResponseHeaders().add("content-type", "json");
        client.sendResponseHeaders(200, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
        client.getResponseBody().write(response.getBytes("UTF-8"));
        client.getResponseBody().close();
        println(jsonReq);
        println(mapsLink);
      }
    }catch{
      case _: Throwable =>
      val response = "{\"error\": \"Opps, ha ocurrido un error :/\"}";
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(500, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
      client.getResponseBody().write(response.getBytes("UTF-8"));
      client.getResponseBody().close();
    }
  }
}

class handler_ejercicio2() extends HttpHandler{
  override def handle(client: HttpExchange){
    try{
      if(client.getRequestMethod() == "POST"){
        val jsonReq = new JsonParser().parse(new String(scala.io.Source.fromInputStream(client.getRequestBody()).mkString)).getAsJsonObject();
        var coordinatesLink = "https://maps.googleapis.com/maps/api/geocode/json?address=ADDRESS&key=AIzaSyDxkk38M1uRTyD6vw7OyBUQ8x_2W2qOsEU";
        try{
          coordinatesLink = coordinatesLink.replace("ADDRESS", jsonReq.get("origen").getAsString().replace(" ", "+"));
        }catch{
          case _: Throwable =>
          val response = "{\"error\": \"No se especifico origen\"}";
          client.getResponseHeaders().add("content-type", "json");
          client.sendResponseHeaders(400, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
          client.getResponseBody().write(response.getBytes("UTF-8"));
          client.getResponseBody().close();
        }
        var conn = (new URL(coordinatesLink).openConnection()).asInstanceOf[HttpURLConnection];
        conn.setRequestMethod("GET");
        println(coordinatesLink);
        var mapsResp = new String(scala.io.Source.fromInputStream(conn.getInputStream()).mkString);
        var nearMeLink = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=LAT,LNG&radius=3000&types=restaurant&name=cruise&key=AIzaSyCx14BVgeJ89yixorOA7gaab-uscUWlNFU";
        nearMeLink = nearMeLink.replace("LAT", new JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsString());
        nearMeLink = nearMeLink.replace("LNG", new JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsString());
        conn = (new URL(nearMeLink).openConnection()).asInstanceOf[HttpURLConnection];
        conn.setRequestMethod("GET");
        println(nearMeLink);
        mapsResp = new String(scala.io.Source.fromInputStream(conn.getInputStream()).mkString);
        var restaurantes = Map[String, ArrayBuffer[JsonObject]]();
        restaurantes("restaurantes") = new ArrayBuffer[JsonObject]();
        val locations = new JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray();
        for(location <- locations){
          var temp = new JsonObject();
          temp.add("nombre", location.getAsJsonObject().get("name"));
          temp.add("lat", location.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat"));
          temp.add("lon", location.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng"));
          restaurantes("restaurantes") += temp;
        }
        var response = new GsonBuilder().create().toJson(restaurantes("restaurantes").asJava);
        response = "{\"restaurantes\":" + response + "}";
        client.getResponseHeaders().add("content-type", "json");
        client.sendResponseHeaders(500, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
        client.getResponseBody().write(response.getBytes("UTF-8"));
        client.getResponseBody().close();
        println(jsonReq);
      }
    }catch{
      case _: Throwable =>
      val response = "{\"error\": \"Opps, ha ocurrido un error :/\"}";
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(500, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
      client.getResponseBody().write(response.getBytes("UTF-8"));
      client.getResponseBody().close();
    }
  }
}

class handler_ejercicio3() extends HttpHandler{
  override def handle(client: HttpExchange){
    try{
      if(client.getRequestMethod() == "POST"){
        val jsonReq = new JsonParser().parse(new String(scala.io.Source.fromInputStream(client.getRequestBody()).mkString)).getAsJsonObject();
        try{
          jsonReq.get("nombre").getAsString();
          jsonReq.get("data").getAsString();
        }catch{
          case _: Throwable =>
          val response = "{\"error\": \"No se especifico origen\"}";
          client.getResponseHeaders().add("content-type", "json");
          client.sendResponseHeaders(400, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
          client.getResponseBody().write(response.getBytes("UTF-8"));
          client.getResponseBody().close();
        }
        var decodedImg = Base64.getDecoder().decode(jsonReq.get("data").getAsString())
        val decodedImgWidth = (ByteBuffer.allocate(4).put(decodedImg.slice(0x12, 0x15).reverse).getInt(0))/256;
        val decodedImgHeight = (ByteBuffer.allocate(4).put(decodedImg.slice(0x16, 0x19).reverse).getInt(0))/256;
        val decodedImgPixelArray = (ByteBuffer.allocate(4).put(decodedImg.slice(0x0A, 0x0D).reverse).getInt(0))/256;

        for(i <- 0 until decodedImgHeight; j <- 0 until decodedImgWidth){
          // println(i, j)
          val pos = decodedImgPixelArray+(i*decodedImgWidth*4)+(j*4);
          val greyPixel = ((decodedImg(pos+3) + decodedImg(pos+2) + decodedImg(pos+1))/3).asInstanceOf[Byte];
          decodedImg(pos) = greyPixel;
          decodedImg(pos+1) = greyPixel;
          decodedImg(pos+2) = greyPixel;
        }

        val encodedImg = Base64.getEncoder().encodeToString(decodedImg);
        val response = "{\"nombre\":\"" + jsonReq.get("nombre").getAsString().replace(".", "(blanco y negro).") + ",\",\"data\":\"" + encodedImg + "\"}";
        client.getResponseHeaders().add("content-type", "json");
        client.sendResponseHeaders(200, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
        client.getResponseBody().write(response.getBytes("UTF-8"));
        client.getResponseBody().close();
      }
    }catch{
      case _: Throwable =>
      val response = "{\"error\": \"Opps, ha ocurrido un error :/\"}";
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(500, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
      client.getResponseBody().write(response.getBytes("UTF-8"));
      client.getResponseBody().close();
    }
  }
}

class handler_ejercicio4() extends HttpHandler{
  override def handle(client: HttpExchange){
    try{
      if(client.getRequestMethod() == "POST"){
        val jsonReq = new JsonParser().parse(new String(scala.io.Source.fromInputStream(client.getRequestBody()).mkString)).getAsJsonObject();
        try{
          jsonReq.get("nombre").getAsString();
          jsonReq.get("data").getAsString();
          jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt();
          jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt();
        }catch{
          case _: Throwable =>
          val response = "{\"error\": \"No se especifico origen\"}";
          client.getResponseHeaders().add("content-type", "json");
          client.sendResponseHeaders(400, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
          client.getResponseBody().write(response.getBytes("UTF-8"));
          client.getResponseBody().close();
        }
        var decodedImg = Base64.getDecoder().decode(jsonReq.get("data").getAsString())
        val decodedImgWidth = (ByteBuffer.allocate(4).put(decodedImg.slice(0x12, 0x15).reverse).getInt(0))/256;
        val decodedImgHeight = (ByteBuffer.allocate(4).put(decodedImg.slice(0x16, 0x19).reverse).getInt(0))/256;
        val decodedImgPixelArray = (ByteBuffer.allocate(4).put(decodedImg.slice(0x0A, 0x0D).reverse).getInt(0))/256;
        val groupWidthBy = decodedImgWidth/jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt();
        val coverWidthFor = decodedImgWidth%jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt();
        val groupHeightBy = decodedImgHeight/jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt();
        val coverHeightFor = decodedImgHeight%jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt();
        printf("Height %d, Width %d\n", jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt(), jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt())

        var resizedImg = new Array[Byte](jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()*jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt()*4+decodedImgPixelArray);
        for(i <- 0 until decodedImgPixelArray)
          resizedImg(i) = decodedImg(i);
        for(i <- 0 until jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt(); j <- 0 until jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()){
          // println(i, j)
          val pos = decodedImgPixelArray+(((groupWidthBy*j)+(if (j<coverWidthFor) j else coverWidthFor))+(decodedImgWidth*((groupHeightBy*i)+(if (i<coverHeightFor) i else coverHeightFor))))*4
          resizedImg(decodedImgPixelArray+i*jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()*4+j*4) = decodedImg(pos);
          resizedImg(decodedImgPixelArray+i*jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()*4+j*4+1) = decodedImg(pos+1);
          resizedImg(decodedImgPixelArray+i*jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()*4+j*4+2) = decodedImg(pos+2);
          resizedImg(decodedImgPixelArray+i*jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt()*4+j*4+3) = decodedImg(pos+3);
          //println(resizedImg.size);
        }

        resizedImg(0x02) = ((resizedImg.size)&0xFF).asInstanceOf[Byte];
        resizedImg(0x03) = (((resizedImg.size)>>8)&0xFF).asInstanceOf[Byte];
        resizedImg(0x04) = (((resizedImg.size)>>16)&0xFF).asInstanceOf[Byte];
        resizedImg(0x05) = ((resizedImg.size)>>24).asInstanceOf[Byte];
        resizedImg(0x12) = ((jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt())&0xFF).asInstanceOf[Byte];
        resizedImg(0x13) = (((jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt())>>8)&0xFF).asInstanceOf[Byte];
        resizedImg(0x14) = (((jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt())>>16)&0xFF).asInstanceOf[Byte];
        resizedImg(0x15) = ((jsonReq.get("tamano").getAsJsonObject().get("ancho").getAsInt())>>24).asInstanceOf[Byte];
        resizedImg(0x16) = ((jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt())&0xFF).asInstanceOf[Byte];
        resizedImg(0x17) = (((jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt())>>8)&0xFF).asInstanceOf[Byte];
        resizedImg(0x18) = (((jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt())>>16)&0xFF).asInstanceOf[Byte];
        resizedImg(0x19) = ((jsonReq.get("tamano").getAsJsonObject().get("alto").getAsInt())>>24).asInstanceOf[Byte];
        resizedImg(0x22) = ((resizedImg.size-decodedImgPixelArray)&0xFF).asInstanceOf[Byte];
        resizedImg(0x23) = (((resizedImg.size-decodedImgPixelArray)>>8)&0xFF).asInstanceOf[Byte];
        resizedImg(0x24) = (((resizedImg.size-decodedImgPixelArray)>>16)&0xFF).asInstanceOf[Byte];
        resizedImg(0x25) = ((resizedImg.size-decodedImgPixelArray)>>24).asInstanceOf[Byte];

        val encodedImg = Base64.getEncoder().encodeToString(resizedImg);
        var response = new GsonBuilder().create().toJson(encodedImg);
        try{
        response = "{\"nombre\":\"" + jsonReq.get("nombre").getAsString().replace(".", "(reducida).") + "\",\"data\":" + response + "}";
        }catch{
          case _: Throwable =>
          val response = "{\"error\": \"No se especifico origen\"}";
          client.getResponseHeaders().add("content-type", "json");
          client.sendResponseHeaders(400, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
          client.getResponseBody().write(response.getBytes("UTF-8"));
          client.getResponseBody().close();
        }
        client.getResponseHeaders().add("content-type", "json");
        client.sendResponseHeaders(200, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
        client.getResponseBody().write(response.getBytes("UTF-8"));
        client.getResponseBody().close();
      }
    }catch{
      case _: Throwable =>
      val response = "{\"error\": \"Opps, ha ocurrido un error :/\"}";
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(500, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
      client.getResponseBody().write(response.getBytes("UTF-8"));
      client.getResponseBody().close();
    }
  }
}