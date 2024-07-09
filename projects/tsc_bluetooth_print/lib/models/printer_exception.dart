sealed class PrinterException {
  const PrinterException(this.message);

  final String message;
}

final class ConnectionException extends PrinterException {
  const ConnectionException(super.message);
}

final class DisconnectionException extends PrinterException {
  const DisconnectionException(super.message);
}

final class PrintingException extends PrinterException {
  const PrintingException(super.message);
}
