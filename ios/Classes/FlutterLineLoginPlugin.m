#import "FlutterLineLoginPlugin.h"
#import <LineSDK/LineSDK.h>

@interface FlutterLineLoginPlugin ()
@property(nonatomic, retain) FlutterMethodChannel *channel;
@end

@implementation FlutterLineLoginPlugin {
    LineSDKAPI *apiClient;
}

+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel =
            [FlutterMethodChannel methodChannelWithName:@"net.granoeste/flutter_line_login"
                                        binaryMessenger:[registrar messenger]];
    FlutterLineLoginPlugin *instance = [[FlutterLineLoginPlugin alloc] init];
    instance.channel = channel;
    [registrar addMethodCallDelegate:instance channel:channel];

    [registrar addApplicationDelegate:instance];
}

- (instancetype)init {
    apiClient = [[LineSDKAPI alloc] initWithConfiguration:[LineSDKConfiguration defaultConfig]];
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // ----------------------
    // MARK: Setting LINE SDK
    // ----------------------
    // Set the LINE Login Delegate
    [LineSDKLogin sharedInstance].delegate = self;

    // Initialize Line SDK API
    apiClient = [[LineSDKAPI alloc] initWithConfiguration:[LineSDKConfiguration defaultConfig]];

    return YES;
}

- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary *)options {
    return [[LineSDKLogin sharedInstance] handleOpenURL:url];
}

// ---------------------------------------------------
// MARK: - MethodCallDelegate for FlutterMethodChannel
// ---------------------------------------------------
- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
    if ([@"startLogin" isEqualToString:call.method]) {
        NSLog(@"Method:startLogin");
        NSArray *permissions = @[@"profile", @"openid", @"email"];
        [[LineSDKLogin sharedInstance] startLoginWithPermissions:permissions];
        result(nil);


    } else if ([@"startWebLogin" isEqualToString:call.method]) {
        NSLog(@"Method:startWebLogin");
        NSArray *permissions = @[@"profile", @"openid", @"email"];
        [[LineSDKLogin sharedInstance] startWebLoginWithPermissions:permissions];
        result(nil);

    } else if ([@"logout" isEqualToString:call.method]) {
        NSLog(@"Method:logout");
        [apiClient logoutWithCompletion:^(BOOL success, NSError *_Nullable error) {
            if (success) {
                result(nil);
            } else {
                // Logout Failed
                NSLog(@"Logout Failed: %@", error.description);
                result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld", error.code]
                                           message:error.domain
                                           details:error.localizedDescription]);
            }
        }];

    } else if ([@"getProfile" isEqualToString:call.method]) {
        NSLog(@"Method:getProfile");
        [apiClient getProfileWithCompletion:^(LineSDKProfile *_Nullable profile, NSError *_Nullable error) {
            if (error) {
                // Error getting profile
                NSLog(@"Error getting profile: %@", error.description);
                result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld", error.code]
                                           message:error.domain
                                           details:error.localizedDescription]);
            } else {
                NSMutableDictionary *resultProfile = [@{
                        @"userID": profile.userID,
                        @"displayName": profile.displayName,
                } mutableCopy];
                if (profile.pictureURL != nil) {
                    resultProfile[@"pictureUrl"] = profile.pictureURL.absoluteString;
                }
                if (profile.statusMessage != nil) {
                    resultProfile[@"statusMessage"] = profile.statusMessage;
                }
                result(resultProfile);
            }
        }];

    } else if ([@"currentAccessToken" isEqualToString:call.method]) {
        NSLog(@"Method:currentAccessToken");
        LineSDKAccessToken *accessToken = [apiClient currentAccessToken];
        if (accessToken != nil) {
            NSMutableDictionary *resultToken = [@{
                    @"accessToken": accessToken.accessToken,
                    @"expiresIn": [NSString stringWithFormat:@"%ld", (NSInteger) accessToken.expiresIn * 1000],
            } mutableCopy];
            result(resultToken);
        } else {
            result(nil);
        }

    } else if ([@"verifyToken" isEqualToString:call.method]) {
        NSLog(@"Method:verifyToken");
        [apiClient verifyTokenWithCompletion:^(LineSDKVerifyResult *_Nullable verifyResult, NSError *_Nullable error) {
            if (error) {
                // Token is invalid
                NSLog(@"Token is Invalid: %@", error.description);
                result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld", error.code]
                                           message:error.domain
                                           details:error.localizedDescription]);
            } else {
                result(nil);
            }
        }];

    } else if ([@"refreshToken" isEqualToString:call.method]) {
        NSLog(@"Method:refreshToken");
        [apiClient refreshTokenWithCompletion:^(LineSDKAccessToken *_Nullable accessToken, NSError *_Nullable error) {
            if (error) {
                // The token refresh failed.
                NSLog(@"Error occurred when refreshing the access token: %@", error.description);
                result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld", error.code]
                                           message:error.domain
                                           details:error.localizedDescription]);
            } else {
                // The token refresh succeeded so we can get the refreshed access token.
                NSMutableDictionary *resultToken = [@{
                        @"accessToken": accessToken.accessToken,
                        @"expiresIn": [NSString stringWithFormat:@"%ld", (NSInteger) accessToken.expiresIn * 1000],
                } mutableCopy];
                result(resultToken);
            }
        }];

    } else {
        result(FlutterMethodNotImplemented);
    }
}

// ---------------------------------------------
// MARK: - LineSDKLoginDelegate
// ---------------------------------------------
- (void)didLogin:(LineSDKLogin *)login
      credential:(LineSDKCredential *)credential
         profile:(LineSDKProfile *)profile
           error:(NSError *)error {

    if (error) {
        NSLog(@"LINE Login Failed with Error: %@", error.description);
        [self.channel invokeMethod:@"loginFailed"
                         arguments:@{
                                     @"code" : [NSString stringWithFormat:@"%ld", error.code],
                                     @"description" : error.localizedDescription,
                                     @"domain" : error.domain,
                                     }];
        return;
    }

    NSLog(@"LINE Login Succeeded");
//    NSLog(@"Access Token: %@", credential.accessToken.accessToken);
//    NSLog(@"User ID: %@", profile.userID);
//    NSLog(@"Display Name: %@", profile.displayName);
//    NSLog(@"Picture URL: %@", profile.pictureURL);
//    NSLog(@"Status Message: %@", profile.statusMessage);

    NSMutableDictionary *result = [@{
            @"userID": profile.userID,
            @"displayName": profile.displayName,
            @"accessToken": credential.accessToken.accessToken,
            @"expiresIn": [NSString stringWithFormat:@"%ld", (NSInteger) credential.accessToken.expiresIn * 1000],
            @"permissions": credential.permissions.array,
    } mutableCopy];
    if (profile.pictureURL != nil) {
        result[@"pictureUrl"] = profile.pictureURL.absoluteString;
    }
    if (profile.statusMessage != nil) {
        result[@"statusMessage"] = profile.statusMessage;
    }

    [self.channel invokeMethod:@"loginSuccess" arguments:result];
}

@end
