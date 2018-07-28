import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_line_login/flutter_line_login.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var _flutterLineLogin = new FlutterLineLogin();

  @override
  void initState() {
    super.initState();
  }

  String _message = '';

  set message(String value) {
    setState(() {
      _message = value;
    });
  }

  void _onLoginSuccess(Object data) {
    message = 'LoginSuccess: ${data}.';
  }

  void _onLoginError(Object error) {
    message = 'LoginError: ${error}.';
  }

  Future<Null> _startLogin() async {
    await _flutterLineLogin.startLogin(_onLoginSuccess, _onLoginError);
  }

  Future<Null> _startWebLogin() async {
    await _flutterLineLogin.startWebLogin(_onLoginSuccess, _onLoginError);
  }

  Future<Null> _logout() async {
    try {
      await _flutterLineLogin.logout();
      message = 'Logout:';
    } on PlatformException catch (e) {
      message = 'Logout: ${e}.';
    }
  }

  Future<Null> _getProfile() async {
    try {
      var profile = await _flutterLineLogin.getProfile();
      message = 'Profile: ${profile}.';
    } on PlatformException catch (e) {
      message = 'Profile: ${e}.';
    }
  }

  Future<Null> _currentAccessToken() async {
    var accessToken = await _flutterLineLogin.currentAccessToken();
    message = 'CurrentAccessToken: ${accessToken}.';
  }

  Future<Null> _verifyToken() async {
    try {
      var result = await _flutterLineLogin.verifyToken();
      message = 'VerifyToken: ${result}.';
    } on PlatformException catch (e) {
      message = 'VerifyToken: ${e}.';
    }
  }

  Future<Null> _refreshToken() async {
    try {
      var result = await _flutterLineLogin.refreshToken();
      message = 'RefreshToken: ${result}.';
    } on PlatformException catch (e) {
      message = 'RefreshToken: ${e}.';
    }
  }

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Flutter Line Login Demo'),
        ),
        body: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  RaisedButton(
                      child: Text('Login'), onPressed: () => _startLogin()),
                  RaisedButton(
                      child: Text('WebLogin'),
                      onPressed: () => _startWebLogin()),
                  RaisedButton(
                      child: Text('Logut'), onPressed: () => _logout()),
                ],
              ),
              RaisedButton(
                  child: Text('GetProfile'), onPressed: () => _getProfile()),
              RaisedButton(
                child: Text('CurrentAccessToken'),
                onPressed: () => _currentAccessToken(),
              ),
              RaisedButton(
                child: Text('VerifyToken'),
                onPressed: () => _verifyToken(),
              ),
              RaisedButton(
                child: Text('RefreshToken'),
                onPressed: () => _refreshToken(),
              ),
              Expanded(
                flex: 1,
                child: SingleChildScrollView(
                  child: Text(_message,
                      style: TextStyle(color: Color.fromARGB(255, 0, 155, 0))),
                ),
              )
            ]),
      ),
    );
  }
}
