package com.thenewmotion.ocpp
package centralsystem

import org.joda.time.DateTime
import scala.concurrent.duration.FiniteDuration

/**
 * @author Yaroslav Klymko
 */
@SerialVersionUID(0)
sealed trait Message
sealed trait Req extends Message
sealed trait Res extends Message


case class AuthorizeReq(idTag: String) extends Req
case class AuthorizeRes(idTag: IdTagInfo) extends Res


case class StartTransactionReq(connector: ConnectorScope,
                               idTag: IdTag,
                               timestamp: DateTime,
                               meterStart: Int,
                               reservationId: Option[Int]) extends Req
case class StartTransactionRes(transactionId: Int, idTag: IdTagInfo) extends Res


case class StopTransactionReq(transactionId: Int,
                              idTag: Option[IdTag],
                              timestamp: DateTime,
                              meterStop: Int,
                              transactionData: List[TransactionData]) extends Req
case class StopTransactionRes(idTag: Option[IdTagInfo]) extends Res


case object HeartbeatReq extends Req
case class HeartbeatRes(currentTime: DateTime) extends Res


case class MeterValuesReq(scope: Scope, transactionId: Option[Int], meters: List[Meter]) extends Req
case object MeterValuesRes extends Res


case class BootNotificationReq(chargePointVendor: String,
                               chargePointModel: String,
                               chargePointSerialNumber: Option[String],
                               chargeBoxSerialNumber: Option[String],
                               firmwareVersion: Option[String],
                               iccid: Option[String],
                               imsi: Option[String],
                               meterType: Option[String],
                               meterSerialNumber: Option[String]) extends Req
case class BootNotificationRes(registrationAccepted: Boolean,
                               currentTime: DateTime /*optional in OCPP 1.2*/ ,
                               heartbeatInterval: FiniteDuration /*optional in OCPP 1.2*/) extends Res


case class StatusNotificationReq(scope: Scope,
                                 status: ChargePointStatus,
                                 timestamp: Option[DateTime],
                                 vendorId: Option[String]) extends Req
case object StatusNotificationRes extends Res


case class FirmwareStatusNotificationReq(status: FirmwareStatus.Value) extends Req
case object FirmwareStatusNotificationRes extends Res


case class DiagnosticsStatusNotificationReq(uploaded: Boolean) extends Req
case object DiagnosticsStatusNotificationRes extends Res


case class DataTransferReq(vendorId: String, messageId: Option[String], data: Option[String]) extends Req
case class DataTransferRes(status: DataTransferStatus.Value, data: Option[String] = None) extends Res

case class TransactionData(meters: List[Meter])

case class Meter(timestamp: DateTime, values: List[Meter.Value] = Nil)

object Meter {
  case class Value(value: String,
                   context: ReadingContext.Value,
                   format: ValueFormat.Value,
                   measurand: Measurand.Value,
                   location: Location.Value,
                   unit: UnitOfMeasure.Value)

  object DefaultValue {
    val readingContext = ReadingContext.SamplePeriodic
    val format = ValueFormat.Raw
    val measurand = Measurand.EnergyActiveImportRegister
    val location = Location.Outlet
    val unitOfMeasure = UnitOfMeasure.Wh

    def apply(value: Int): Value = Value(value.toString, readingContext, format, measurand, location, unitOfMeasure)

    def unapply(x: Value): Option[Int] = PartialFunction.condOpt(x) {
      case Value(value, `readingContext`, `format`, `measurand`, `location`, `unitOfMeasure`) => value.toFloat.round
    }
  }

  object Location extends Enumeration {
    val Inlet = Value(0, "Inlet")
    val Outlet = Value(1, "Outlet")
    val Body = Value(2, "Body")
  }

  object Measurand extends Enumeration {
    val EnergyActiveExportRegister = Value(0, "Energy.Active.Export.Register")
    val EnergyActiveImportRegister = Value(1, "Energy.Active.Import.Register")
    val EnergyReactiveExportRegister = Value(2, "Energy.Reactive.Export.Register")
    val EnergyReactiveImportRegister = Value(3, "Energy.ReactiveImport.Register")
    val EnergyActiveExportInterval = Value(4, "Energy.Active.Export.Interval")
    val EnergyActiveImportInterval = Value(5, "Energy.Active.Import.Interval")
    val EnergyReactiveExportInterval = Value(6, "Energy.Reactive.Export.Interval")
    val EnergyReactiveImportInterval = Value(7, "Energy.Reactive.Import.Interval")
    val PowerActiveExport = Value(8, "Power.Active.Export")
    val PowerActiveImport = Value(9, "Power.Active.Import")
    val PowerReactiveExport = Value(10, "Power.Reactive.Export")
    val PowerReactiveImport = Value(11, "Power.Reactive.Import")
    val CurrentExport = Value(12, "Current.Export")
    val CurrentImport = Value(13, "Current.Import")
    val Voltage = Value(14, "Voltage")
    val Temperature = Value(15, "Temperature")
  }

  object ValueFormat extends Enumeration {
    val Raw = Value(0, "Raw")
    val Signed = Value(1, "Signed")
  }

  object ReadingContext extends Enumeration {
    val InterruptionBegin = Value(0, "Interruption.Begin")
    val InterruptionEnd = Value(1, "Interruption.End")
    val SampleClock = Value(2, "Sample.Clock")
    val SamplePeriodic = Value(3, "Sample.Periodic")
    val TransactionBegin = Value(4, "Transaction.Begin")
    val TransactionEnd = Value(5, "Transaction.End")
  }

  object UnitOfMeasure extends Enumeration {
    val Wh = Value(0, "Wh")
    val Kwh = Value(1, "kWh")
    val Varh = Value(2, "varh")
    val Kvarh = Value(3, "kvarh")
    val W = Value(4, "W")
    val Kw = Value(5, "kW")
    val Var = Value(6, "var")
    val Kvar = Value(7, "kvar")
    val Amp = Value(8, "Amp")
    val Volt = Value(9, "Volt")
    val Celsius = Value(10, "Celsius")
  }
}

sealed trait ChargePointStatus
case object Available extends ChargePointStatus
case object Occupied extends ChargePointStatus
case class Faulted(errorCode: Option[ChargePointErrorCode.Value],
                   info: Option[String],
                   vendorErrorCode: Option[String]) extends ChargePointStatus
case object Unavailable extends ChargePointStatus
// since OCPP 1.5
case object Reserved extends ChargePointStatus

object ChargePointErrorCode extends Enumeration {
  val ConnectorLockFailure,
  HighTemperature,
  Mode3Error,
  PowerMeterFailure,
  PowerSwitchFailure,
  ReaderFailure,
  ResetFailure,
  GroundFailure /*since OCPP 1.5*/ ,
  OverCurrentFailure,
  UnderVoltage,
  WeakSignal,
  OtherError = Value
}

object FirmwareStatus extends Enumeration {
  val Downloaded,
  DownloadFailed,
  InstallationFailed,
  Installed = Value
}