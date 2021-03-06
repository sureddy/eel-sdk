package io.eels.component.parquet

import com.sksamuel.exts.metrics.Timed
import io.eels.component.parquet.avro.{AvroParquetSource, ParquetSink}
import io.eels.schema.StructType
import io.eels.{Frame, Row}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.util.Random

/**
  * v0.90 1m rows insertion: 2500 reading: 4438
  * v1.1.0-snapshot-1st-dec 1m rows insertion 3400 reading: 2800
  * 1m rows string of length 4; writing=3397; reading=1186
  * 2m rows string of length 4; writing=7725; reading=5479
  * 2m rows string of length 4; writing=7766; reading=5177 dictionary enabled for read
  * v1.1.0-snapshot-7th-dec
  * 2m rows string of length 4; writing=5964; reading=4193
  * v.1.1-snapshot-13th dec, flux
  * 2m rows string of length 4; writing=6082; reading parquet=4692; reading avro=4401
  * v.1.1-snapshot-13th dec, closeable iterator
  * 2m rows string of length 4; writing=; reading parquet=5555; reading avro=
  */
object ParquetSpeedTest extends App with Timed {
  ParquetLogMute()

  val schema = StructType("a", "b", "c", "d", "e")
  val rows = List.fill(2000000)(Row(schema, Random.nextBoolean(), Random.nextFloat(), Random.nextGaussian(), Random.nextLong(), Random.nextString(4)))
  val frame = Frame(schema, rows)

  implicit val conf = new Configuration()
  implicit val fs = FileSystem.getLocal(new Configuration())

  val path = new Path("parquet_speed.csv")
  fs.delete(path, false)

  timed("Insertion") {
    frame.to(ParquetSink(path))
  }

  while (true) {

    timed("Reading with ParquetSource") {
      val in = ParquetSource(path).toFrame().collect()
      assert(in.size == rows.size, in.size)
    }

    timed("Reading with AvroParquetSource") {
      val in = AvroParquetSource(path).toFrame().collect()
      assert(in.size == rows.size, in.size)
    }

  }
}
