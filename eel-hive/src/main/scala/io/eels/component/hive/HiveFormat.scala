package io.eels.component.hive

import org.apache.hadoop.hive.ql.io.orc.{OrcInputFormat, OrcOutputFormat, OrcSerde}
import org.apache.hadoop.hive.ql.io.parquet.{MapredParquetInputFormat, MapredParquetOutputFormat}
import org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe

trait HiveFormat {
  def serde: String
  def inputFormat: String
  def outputFormat: String
}

object HiveFormat {

  object Text extends HiveFormat {
    override def serde: String = "org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"
    override def inputFormat: String = "org.apache.hadoop.mapred.TextInputFormat"
    override def outputFormat: String = "org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat"
  }

  object Parquet extends HiveFormat {
    override val serde: String = classOf[ParquetHiveSerDe].getCanonicalName
    override val inputFormat: String = classOf[MapredParquetInputFormat].getCanonicalName
    override val outputFormat: String = classOf[MapredParquetOutputFormat].getCanonicalName
  }

  object Avro extends HiveFormat {
    override def serde: String = "org.apache.hadoop.hive.serde2.avro.AvroSerDe"
    override def inputFormat: String = "org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat"
    override def outputFormat: String = "org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat"
  }

  object Orc extends HiveFormat {
    override val serde: String = classOf[OrcSerde].getCanonicalName
    override val inputFormat: String = classOf[OrcInputFormat].getCanonicalName
    override val outputFormat: String = classOf[OrcOutputFormat].getCanonicalName
  }

  def apply(format: String): HiveFormat = format match {
    case "avro" => HiveFormat.Avro
    case "orc" => HiveFormat.Orc
    case "parquet" => HiveFormat.Parquet
    case "text" => HiveFormat.Text
    case _ => throw new UnsupportedOperationException(s"Unknown hive input format $format")
  }

  def fromInputFormat(inputFormat: String): HiveFormat = inputFormat match {
    case "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat" => Parquet
    case "org.apache.hadoop.mapred.TextInputFormat" => Text
    case "org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat" => Avro
    case "org.apache.hadoop.hive.ql.io.orc.OrcInputFormat" => Orc
    case _ => throw new UnsupportedOperationException(s"Input format not known $inputFormat")
  }
}