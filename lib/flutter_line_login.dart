import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';

class FlutterLineLogin {
  static const MethodChannel _methodChannel =
      const MethodChannel('net.granoeste/flutter_line_login');
  static const EventChannel _eventChannel =
      const EventChannel('net.granoeste/flutter_line_login_result');

  FlutterLineLogin() {
    _eventChannel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
  }

  Function _onLoginSuccess;
  Function _onLoginError;

  void _onEvent(Object event) {
    _onLoginSuccess(event);
  }

  void _onError(Object error) {
    _onLoginError(error);
  }


  startLogin(Function onSuccess, Function onError) async {
    _onLoginSuccess = onSuccess;
    _onLoginError = onError;
    await _methodChannel.invokeMethod('startLogin');
  }

  startWebLogin(Function onSuccess, Function onError) async {
    _onLoginSuccess = onSuccess;
    _onLoginError = onError;
    await _methodChannel.invokeMethod('startWebLogin');
  }

  logout() async {
    try {
      await _methodChannel.invokeMethod('logout');
    } on PlatformException catch (e) {
      rethrow;
    }
  }

  getProfile() async {
    try {
      return await _methodChannel.invokeMethod('getProfile');
    } on PlatformException catch (e) {
      rethrow;
    }
  }

  currentAccessToken() async {
    return await _methodChannel.invokeMethod('currentAccessToken');
  }

  verifyToken() async {
    try {
      return await _methodChannel.invokeMethod('verifyToken');
    } on PlatformException catch (e) {
      rethrow;
    }
  }

  refreshToken() async {
    try {
      return await _methodChannel.invokeMethod('refreshToken');
    } on PlatformException catch (e) {
      rethrow;
    }
  }
}
