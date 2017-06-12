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
      // server.createContext("/ejercicio2", new handler_ejercicio2());
      // server.createContext("/ejercicio3", new handler_ejercicio3());
      server.start();
      println("Server up!");
  }
}

class handler_ejercicio1() extends HttpHandler{
  override def handle(client: HttpExchange){
    if(client.getRequestMethod() == "POST"){
      val jsonReq = new JsonParser().parse(new String(scala.io.Source.fromInputStream(client.getRequestBody()).mkString)).getAsJsonObject();
      var mapsLink = "https://maps.googleapis.com/maps/api/directions/json?origin=ORIGIN&destination=DESTINATION&key=AIzaSyA7sQSQEOesLMKtCLmqISRpv7YHeWL67-c";
      mapsLink = mapsLink.replace("ORIGIN", jsonReq.get("origen").getAsString().replace(" ", "+"));
      mapsLink = mapsLink.replace("DESTINATION", jsonReq.get("destino").getAsString().replace(" ", "+"));
      val conn = (new URL(mapsLink).openConnection()).asInstanceOf[HttpURLConnection];
      conn.setRequestMethod("GET");
      val mapsResp = new String(scala.io.Source.fromInputStream(conn.getInputStream()).mkString);
      val routeOnMaps = new JsonParser().parse(mapsResp).getAsJsonObject().get("routes").getAsJsonArray().get(0).getAsJsonObject().get("legs").getAsJsonArray().get(0).getAsJsonObject().get("steps").getAsJsonArray();
      var ruta = Map[String, ArrayBuffer[JsonObject]]();
      ruta("ruta") = new ArrayBuffer[JsonObject]();
      //println(routeOnMaps);
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
      val response = new GsonBuilder().create().toJson(ruta("ruta").asJava);
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(200, response.getBytes("UTF-8").size.asInstanceOf[Number].longValue);
      client.getResponseBody().write(response.getBytes("UTF-8"));
      client.getResponseBody().close();
      println(jsonReq);
      println(mapsLink);
    }
  }
}

/*class handler_ejercicio2() extends HttpHandler{
  override def handle(client: HttpExchange){
    if(client.getRequestMethod() == "POST"){
      val jsonReq = JsonParser().parse(String(client.getRequestBody().readBytes())).getAsJsonObject();
      var coordinatesLink = "https://maps.googleapis.com/maps/api/geocode/json?address=ADDRESS&key=AIzaSyDxkk38M1uRTyD6vw7OyBUQ8x_2W2qOsEU";
      coordinatesLink = coordinatesLink.replace("ADDRESS", jsonReq.get("origen").getAsString().replace(" ", "+"));
      var conn = URL(coordinatesLink).openConnection() as HttpURLConnection;
      println(coordinatesLink);
      conn.setRequestMethod("GET");
      var mapsResp = String(conn.getInputStream().readBytes());
      var nearMeLink = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=LAT,LNG&radius=3000&types=food&name=cruise&key=AIzaSyCx14BVgeJ89yixorOA7gaab-uscUWlNFU";
      nearMeLink = nearMeLink.replace("LAT", JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsString());
      nearMeLink = nearMeLink.replace("LNG", JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsString());
      conn = URL(nearMeLink).openConnection() as HttpURLConnection;
      conn.setRequestMethod("GET");
      println(nearMeLink);
      mapsResp = String(conn.getInputStream().readBytes());
      var restaurantes = mutableMapOf<String, ArrayList<JsonObject>>();
      restaurantes.put("restaurantes", ArrayList<JsonObject>());
      for(location in JsonParser().parse(mapsResp).getAsJsonObject().get("results").getAsJsonArray()){
        var temp = JsonObject();
        temp.add("nombre", location.getAsJsonObject().get("name"));
        temp.add("lat", location.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat"));
        temp.add("lon", location.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng"));
        restaurantes["restaurantes"]?.add(temp);
      }
      val response = GsonBuilder().create().toJson(restaurantes).toByteArray();
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(200, response.size.toLong());
      client.getResponseBody().write(response);
      client.getResponseBody().close();
      println(jsonReq);
    }
  }
}

class handler_ejercicio3() extends HttpHandler{
  override def handle(client: HttpExchange){
    if(client.getRequestMethod() == "POST"){
      val jsonReq = JsonParser().parse(String(client.getRequestBody().readBytes())).getAsJsonObject();
      var decodedImg = Base64.getDecoder().decode(jsonReq.get("data").getAsString())
      val decodedImgWidth = ByteBuffer.wrap(decodedImg.sliceArray(IntRange(0x12, 0x15)).reversedArray()).getInt();
      val decodedImgHeight = ByteBuffer.wrap(decodedImg.sliceArray(IntRange(0x16, 0x19)).reversedArray()).getInt();
      val decodedImgPixelArray = ByteBuffer.wrap(decodedImg.sliceArray(IntRange(0x0A, 0x0D)).reversedArray()).getInt();

      for(i in 0 until decodedImgHeight){
        for(j in 0 until decodedImgWidth){
          val pos = decodedImgPixelArray+(i*decodedImgWidth*4)+(j*4);
          val greyPixel = ((decodedImg[pos+3] + decodedImg[pos+2] + decodedImg[pos+1])/3).toByte()
          decodedImg[pos] = greyPixel
          decodedImg[pos+1] = greyPixel
          decodedImg[pos+2] = greyPixel
        }
      }

      var encodedImg = mutableMapOf<String, String>();
      encodedImg.put("data", String(Base64.getEncoder().encode(decodedImg)))
      val response = GsonBuilder().create().toJson(encodedImg).toByteArray();
      client.getResponseHeaders().add("content-type", "json");
      client.sendResponseHeaders(200, response.size.toLong());
      client.getResponseBody().write(response);
      client.getResponseBody().close();
    }
  }
}*/