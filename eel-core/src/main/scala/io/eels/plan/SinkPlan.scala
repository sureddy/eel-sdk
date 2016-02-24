package io.eels.plan

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.typesafe.scalalogging.slf4j.StrictLogging
import io.eels.{Frame, Sink}

import scala.concurrent.{ExecutionContext, Future}

object SinkPlan extends Plan with StrictLogging {

  def apply(sink: Sink, frame: Frame)(implicit execution: ExecutionContext): Long = {

    val count = new AtomicLong(0)
    val latch = new CountDownLatch(slices)
    val running = new AtomicBoolean(true)
    val schema = frame.schema
    val buffer = frame.buffer
    val writer = sink.writer

    for (k <- 1 to slices) {
      Future {
        try {
          buffer.iterator.takeWhile(_ => running.get).foreach { row =>
            writer.write(row, schema)
            count.incrementAndGet()
          }
        } catch {
          case e: Throwable =>
            logger.error("Error writing; shutting down executor", e)
            throw e
        } finally {
          latch.countDown()
        }
      }
    }

    logger.debug(s"Waiting ${timeout.toMillis}ms for sink to complete")
    latch.await(timeout.toNanos, TimeUnit.NANOSECONDS)
    logger.debug("Closing buffer")
    writer.close()
    buffer.close()
    logger.debug("Buffer closed")

    count.get()
  }
}