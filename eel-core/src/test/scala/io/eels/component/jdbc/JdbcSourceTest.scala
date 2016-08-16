package io.eels.component.jdbc

import java.sql.DriverManager

import io.eels.schema.{Field, FieldType, Precision, Schema}
import org.scalatest.{Matchers, WordSpec}

class JdbcSourceTest extends WordSpec with Matchers {

  Class.forName("org.h2.Driver")

  "JdbcSource" should {
    "read schema" in {
      val conn = DriverManager.getConnection("jdbc:h2:mem:test")
      conn.createStatement().executeUpdate("create table mytable (a integer, b bit, c bigint)")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('1','2','3')")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('4','5','6')")
      JdbcSource("jdbc:h2:mem:test", "select * from mytable").schema() shouldBe
        Schema(
          Field("A", FieldType.Int, true, Precision(10), signed = true),
          Field("B", FieldType.Boolean, true, Precision(1), signed = true),
          Field("C", FieldType.Long, true, Precision(19), signed = true)
        )
    }
    "use supplied query" in {
      val conn = DriverManager.getConnection("jdbc:h2:mem:test3")
      conn.createStatement().executeUpdate("create table mytable (a integer, b bit, c bigint)")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('1','2','3')")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('4','5','6')")
      JdbcSource(() => DriverManager.getConnection("jdbc:h2:mem:test3"), "select * from mytable where a=4").toFrame(1).size() shouldBe 1
      val a = JdbcSource("jdbc:h2:mem:test3", "select a,c from mytable where a=4").toFrame(1).toList()
      a.head.values.head shouldBe 4
      a.head.values(1) shouldBe 6L
    }
    "read from jdbc" in {
      val conn = DriverManager.getConnection("jdbc:h2:mem:test4")
      conn.createStatement().executeUpdate("create table mytable (a integer, b bit, c bigint)")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('1','2','3')")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('4','5','6')")
      JdbcSource("jdbc:h2:mem:test4", "select * from mytable").toFrame(1).size() shouldBe 2
    }
    "support bind" in {
      val conn = DriverManager.getConnection("jdbc:h2:mem:test5")
      conn.createStatement().executeUpdate("create table mytable (a integer, b bit, c bigint)")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('1','2','3')")
      conn.createStatement().executeUpdate("insert into mytable (a,b,c) values ('4','5','6')")
      JdbcSource("jdbc:h2:mem:test5", "select * from mytable where a=?").withBind { it =>
        it.setLong(1, 4)
      }.toFrame(1).size() shouldBe 1
    }
  }
}
