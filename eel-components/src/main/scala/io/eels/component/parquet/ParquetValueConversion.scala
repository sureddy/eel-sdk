package io.eels.component.parquet

import io.eels.schema._
import org.apache.parquet.io.api.{Binary, RecordConsumer}

// accepts a scala/java value and writes it out to a record consumer as the appropriate
// parquet value for the given schema type
trait ParquetValueConversion {
  def write(record: RecordConsumer, value: Any)
}

object ParquetValueConversion {
  def apply(dataType: DataType): ParquetValueConversion = {
    dataType match {
      case BigIntType => BigIntParquetValueConversion
      case BooleanType => BooleanParquetValueConversion
      case DateType => DateParquetValueConversion
      case DoubleType => DoubleParquetValueConversion
      case FloatType => FloatParquetValueConversion
      case _: IntType => IntParquetValueWriter
      case _: LongType => LongParquetValueWriter
      case StringType => StringParquetValueConversion
      case TimeType => TimeParquetValueConversion
      case TimestampType => TimestampParquetValueConversion
    }
  }
}

object BigIntParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addLong(value.asInstanceOf[BigInt].toLong)
  }
}

object DateParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addInteger(value.toString.toInt)
  }
}

object TimeParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addLong(value.toString.toLong)
  }
}

object TimestampParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addLong(value.toString.toLong)
  }
}

object StringParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addBinary(Binary.fromString(value.toString))
  }
}

object DoubleParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addDouble(value.asInstanceOf[Double])
  }
}

object FloatParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addFloat(value.asInstanceOf[Float])
  }
}

object BooleanParquetValueConversion extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addBoolean(value.asInstanceOf[Boolean])
  }
}

object LongParquetValueWriter extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addLong(value.asInstanceOf[Long])
  }
}

object IntParquetValueWriter extends ParquetValueConversion {
  override def write(record: RecordConsumer, value: Any): Unit = {
    record.addInteger(value.asInstanceOf[Int])
  }
}