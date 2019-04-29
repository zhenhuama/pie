// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: bdars.proto

// This CPP symbol can be defined to use imports that match up to the framework
// imports needed when using CocoaPods.
#if !defined(GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS)
 #define GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS 0
#endif

#if GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS
 #import <Protobuf/GPBProtocolBuffers.h>
#else
 #import "GPBProtocolBuffers.h"
#endif

#if GOOGLE_PROTOBUF_OBJC_VERSION < 30002
#error This file was generated by a newer version of protoc which is incompatible with your Protocol Buffer library sources.
#endif
#if 30002 < GOOGLE_PROTOBUF_OBJC_MIN_SUPPORTED_VERSION
#error This file was generated by an older version of protoc which is incompatible with your Protocol Buffer library sources.
#endif

// @@protoc_insertion_point(imports)

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

CF_EXTERN_C_BEGIN

NS_ASSUME_NONNULL_BEGIN

#pragma mark - BDABdarsRoot

/**
 * Exposes the extension registry for this file.
 *
 * The base class provides:
 * @code
 *   + (GPBExtensionRegistry *)extensionRegistry;
 * @endcode
 * which is a @c GPBExtensionRegistry that includes all the extensions defined by
 * this file and all files that it depends on.
 **/
@interface BDABdarsRoot : GPBRootObject
@end

#pragma mark - BDAInitRequest

typedef GPB_ENUM(BDAInitRequest_FieldNumber) {
  BDAInitRequest_FieldNumber_Enablelongspeech = 1,
  BDAInitRequest_FieldNumber_Enablechunk = 2,
  BDAInitRequest_FieldNumber_EnableflushData = 3,
  BDAInitRequest_FieldNumber_ProductId = 4,
  BDAInitRequest_FieldNumber_SamplePointBytes = 5,
  BDAInitRequest_FieldNumber_SendPerSeconds = 6,
  BDAInitRequest_FieldNumber_SleepRatio = 7,
  BDAInitRequest_FieldNumber_AppName = 8,
  BDAInitRequest_FieldNumber_LogLevel = 9,
};

/**
 * InitRequest
 **/
@interface BDAInitRequest : GPBMessage

@property(nonatomic, readwrite) BOOL enablelongspeech;

@property(nonatomic, readwrite) BOOL enablechunk;

@property(nonatomic, readwrite) BOOL enableflushData;

@property(nonatomic, readwrite, copy, null_resettable) NSString *productId;

@property(nonatomic, readwrite) uint32_t samplePointBytes;

@property(nonatomic, readwrite) double sendPerSeconds;

@property(nonatomic, readwrite) double sleepRatio;

@property(nonatomic, readwrite, copy, null_resettable) NSString *appName;

@property(nonatomic, readwrite) uint32_t logLevel;

@end

#pragma mark - BDAAudioFragmentRequest

typedef GPB_ENUM(BDAAudioFragmentRequest_FieldNumber) {
  BDAAudioFragmentRequest_FieldNumber_AudioData = 1,
};

@interface BDAAudioFragmentRequest : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSData *audioData;

@end

#pragma mark - BDAAudioFragmentResponse

typedef GPB_ENUM(BDAAudioFragmentResponse_FieldNumber) {
  BDAAudioFragmentResponse_FieldNumber_Errorcode = 1,
  BDAAudioFragmentResponse_FieldNumber_Errormessage = 2,
  BDAAudioFragmentResponse_FieldNumber_Starttime = 3,
  BDAAudioFragmentResponse_FieldNumber_Endtime = 4,
  BDAAudioFragmentResponse_FieldNumber_Result = 5,
  BDAAudioFragmentResponse_FieldNumber_Completed = 6,
  BDAAudioFragmentResponse_FieldNumber_SerialNum = 7,
};

@interface BDAAudioFragmentResponse : GPBMessage

@property(nonatomic, readwrite) int32_t errorcode;

@property(nonatomic, readwrite, copy, null_resettable) NSString *errormessage;

@property(nonatomic, readwrite, copy, null_resettable) NSString *starttime;

@property(nonatomic, readwrite, copy, null_resettable) NSString *endtime;

@property(nonatomic, readwrite, copy, null_resettable) NSString *result;

@property(nonatomic, readwrite) BOOL completed;

@property(nonatomic, readwrite, copy, null_resettable) NSString *serialNum;

@end

NS_ASSUME_NONNULL_END

CF_EXTERN_C_END

#pragma clang diagnostic pop

// @@protoc_insertion_point(global_scope)