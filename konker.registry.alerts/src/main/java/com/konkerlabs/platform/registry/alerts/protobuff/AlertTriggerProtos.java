package com.konkerlabs.platform.registry.alerts.protobuff;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageOrBuilder;

public final class AlertTriggerProtos {
  private AlertTriggerProtos() {}
  public static void registerAllExtensions(
      ExtensionRegistry registry) {
  }
  public interface AlertTriggerOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger)
      MessageOrBuilder {

    /**
     * <code>required string id = 1;</code>
     */
    boolean hasId();
    /**
     * <code>required string id = 1;</code>
     */
    java.lang.String getId();
    /**
     * <code>required string id = 1;</code>
     */
    com.google.protobuf.ByteString
        getIdBytes();

    /**
     * <code>required string guid = 2;</code>
     */
    boolean hasGuid();
    /**
     * <code>required string guid = 2;</code>
     */
    java.lang.String getGuid();
    /**
     * <code>required string guid = 2;</code>
     */
    com.google.protobuf.ByteString
        getGuidBytes();

    /**
     * <code>optional string description = 3;</code>
     */
    boolean hasDescription();
    /**
     * <code>optional string description = 3;</code>
     */
    java.lang.String getDescription();
    /**
     * <code>optional string description = 3;</code>
     */
    com.google.protobuf.ByteString
        getDescriptionBytes();

    /**
     * <code>required string tenant = 4;</code>
     */
    boolean hasTenant();
    /**
     * <code>required string tenant = 4;</code>
     */
    java.lang.String getTenant();
    /**
     * <code>required string tenant = 4;</code>
     */
    com.google.protobuf.ByteString
        getTenantBytes();

    /**
     * <code>required string application = 5;</code>
     */
    boolean hasApplication();
    /**
     * <code>required string application = 5;</code>
     */
    java.lang.String getApplication();
    /**
     * <code>required string application = 5;</code>
     */
    com.google.protobuf.ByteString
        getApplicationBytes();

    /**
     * <code>required string deviceModel = 6;</code>
     */
    boolean hasDeviceModel();
    /**
     * <code>required string deviceModel = 6;</code>
     */
    java.lang.String getDeviceModel();
    /**
     * <code>required string deviceModel = 6;</code>
     */
    com.google.protobuf.ByteString
        getDeviceModelBytes();

    /**
     * <code>required string location = 7;</code>
     */
    boolean hasLocation();
    /**
     * <code>required string location = 7;</code>
     */
    java.lang.String getLocation();
    /**
     * <code>required string location = 7;</code>
     */
    com.google.protobuf.ByteString
        getLocationBytes();

    /**
     * <code>required string type = 8;</code>
     */
    boolean hasType();
    /**
     * <code>required string type = 8;</code>
     */
    java.lang.String getType();
    /**
     * <code>required string type = 8;</code>
     */
    com.google.protobuf.ByteString
        getTypeBytes();
  }
  /**
   * Protobuf type {@code com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger}
   */
  public static final class AlertTrigger extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger)
      AlertTriggerOrBuilder {
    // Use AlertTrigger.newBuilder() to construct.
    private AlertTrigger(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private AlertTrigger(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final AlertTrigger defaultInstance;
    public static AlertTrigger getDefaultInstance() {
      return defaultInstance;
    }

    public AlertTrigger getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private AlertTrigger(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              id_ = bs;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000002;
              guid_ = bs;
              break;
            }
            case 26: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000004;
              description_ = bs;
              break;
            }
            case 34: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000008;
              tenant_ = bs;
              break;
            }
            case 42: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000010;
              application_ = bs;
              break;
            }
            case 50: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000020;
              deviceModel_ = bs;
              break;
            }
            case 58: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000040;
              location_ = bs;
              break;
            }
            case 66: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000080;
              type_ = bs;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return AlertTriggerProtos.internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return AlertTriggerProtos.internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              AlertTriggerProtos.AlertTrigger.class, AlertTriggerProtos.AlertTrigger.Builder.class);
    }

    public static com.google.protobuf.Parser<AlertTrigger> PARSER =
        new com.google.protobuf.AbstractParser<AlertTrigger>() {
      public AlertTrigger parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new AlertTrigger(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<AlertTrigger> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    public static final int ID_FIELD_NUMBER = 1;
    private java.lang.Object id_;
    /**
     * <code>required string id = 1;</code>
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string id = 1;</code>
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          id_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string id = 1;</code>
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int GUID_FIELD_NUMBER = 2;
    private java.lang.Object guid_;
    /**
     * <code>required string guid = 2;</code>
     */
    public boolean hasGuid() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required string guid = 2;</code>
     */
    public java.lang.String getGuid() {
      java.lang.Object ref = guid_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          guid_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string guid = 2;</code>
     */
    public com.google.protobuf.ByteString
        getGuidBytes() {
      java.lang.Object ref = guid_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        guid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int DESCRIPTION_FIELD_NUMBER = 3;
    private java.lang.Object description_;
    /**
     * <code>optional string description = 3;</code>
     */
    public boolean hasDescription() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>optional string description = 3;</code>
     */
    public java.lang.String getDescription() {
      java.lang.Object ref = description_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          description_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string description = 3;</code>
     */
    public com.google.protobuf.ByteString
        getDescriptionBytes() {
      java.lang.Object ref = description_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        description_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int TENANT_FIELD_NUMBER = 4;
    private java.lang.Object tenant_;
    /**
     * <code>required string tenant = 4;</code>
     */
    public boolean hasTenant() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>required string tenant = 4;</code>
     */
    public java.lang.String getTenant() {
      java.lang.Object ref = tenant_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          tenant_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string tenant = 4;</code>
     */
    public com.google.protobuf.ByteString
        getTenantBytes() {
      java.lang.Object ref = tenant_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tenant_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int APPLICATION_FIELD_NUMBER = 5;
    private java.lang.Object application_;
    /**
     * <code>required string application = 5;</code>
     */
    public boolean hasApplication() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>required string application = 5;</code>
     */
    public java.lang.String getApplication() {
      java.lang.Object ref = application_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          application_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string application = 5;</code>
     */
    public com.google.protobuf.ByteString
        getApplicationBytes() {
      java.lang.Object ref = application_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        application_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int DEVICEMODEL_FIELD_NUMBER = 6;
    private java.lang.Object deviceModel_;
    /**
     * <code>required string deviceModel = 6;</code>
     */
    public boolean hasDeviceModel() {
      return ((bitField0_ & 0x00000020) == 0x00000020);
    }
    /**
     * <code>required string deviceModel = 6;</code>
     */
    public java.lang.String getDeviceModel() {
      java.lang.Object ref = deviceModel_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          deviceModel_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string deviceModel = 6;</code>
     */
    public com.google.protobuf.ByteString
        getDeviceModelBytes() {
      java.lang.Object ref = deviceModel_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        deviceModel_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int LOCATION_FIELD_NUMBER = 7;
    private java.lang.Object location_;
    /**
     * <code>required string location = 7;</code>
     */
    public boolean hasLocation() {
      return ((bitField0_ & 0x00000040) == 0x00000040);
    }
    /**
     * <code>required string location = 7;</code>
     */
    public java.lang.String getLocation() {
      java.lang.Object ref = location_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          location_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string location = 7;</code>
     */
    public com.google.protobuf.ByteString
        getLocationBytes() {
      java.lang.Object ref = location_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        location_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int TYPE_FIELD_NUMBER = 8;
    private java.lang.Object type_;
    /**
     * <code>required string type = 8;</code>
     */
    public boolean hasType() {
      return ((bitField0_ & 0x00000080) == 0x00000080);
    }
    /**
     * <code>required string type = 8;</code>
     */
    public java.lang.String getType() {
      java.lang.Object ref = type_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          type_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string type = 8;</code>
     */
    public com.google.protobuf.ByteString
        getTypeBytes() {
      java.lang.Object ref = type_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        type_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private void initFields() {
      id_ = "";
      guid_ = "";
      description_ = "";
      tenant_ = "";
      application_ = "";
      deviceModel_ = "";
      location_ = "";
      type_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasGuid()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasTenant()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasApplication()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasDeviceModel()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasLocation()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasType()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getGuidBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeBytes(3, getDescriptionBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeBytes(4, getTenantBytes());
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeBytes(5, getApplicationBytes());
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        output.writeBytes(6, getDeviceModelBytes());
      }
      if (((bitField0_ & 0x00000040) == 0x00000040)) {
        output.writeBytes(7, getLocationBytes());
      }
      if (((bitField0_ & 0x00000080) == 0x00000080)) {
        output.writeBytes(8, getTypeBytes());
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getGuidBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, getDescriptionBytes());
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(4, getTenantBytes());
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(5, getApplicationBytes());
      }
      if (((bitField0_ & 0x00000020) == 0x00000020)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(6, getDeviceModelBytes());
      }
      if (((bitField0_ & 0x00000040) == 0x00000040)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(7, getLocationBytes());
      }
      if (((bitField0_ & 0x00000080) == 0x00000080)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(8, getTypeBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static AlertTriggerProtos.AlertTrigger parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static AlertTriggerProtos.AlertTrigger parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static AlertTriggerProtos.AlertTrigger parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static AlertTriggerProtos.AlertTrigger parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(AlertTriggerProtos.AlertTrigger prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger)
        AlertTriggerProtos.AlertTriggerOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return AlertTriggerProtos.internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return AlertTriggerProtos.internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                AlertTriggerProtos.AlertTrigger.class, AlertTriggerProtos.AlertTrigger.Builder.class);
      }

      // Construct using AlertTriggerProtos.AlertTrigger.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        id_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        guid_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        description_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        tenant_ = "";
        bitField0_ = (bitField0_ & ~0x00000008);
        application_ = "";
        bitField0_ = (bitField0_ & ~0x00000010);
        deviceModel_ = "";
        bitField0_ = (bitField0_ & ~0x00000020);
        location_ = "";
        bitField0_ = (bitField0_ & ~0x00000040);
        type_ = "";
        bitField0_ = (bitField0_ & ~0x00000080);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return AlertTriggerProtos.internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor;
      }

      public AlertTriggerProtos.AlertTrigger getDefaultInstanceForType() {
        return AlertTriggerProtos.AlertTrigger.getDefaultInstance();
      }

      public AlertTriggerProtos.AlertTrigger build() {
        AlertTriggerProtos.AlertTrigger result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public AlertTriggerProtos.AlertTrigger buildPartial() {
        AlertTriggerProtos.AlertTrigger result = new AlertTriggerProtos.AlertTrigger(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.id_ = id_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.guid_ = guid_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.description_ = description_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.tenant_ = tenant_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.application_ = application_;
        if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
          to_bitField0_ |= 0x00000020;
        }
        result.deviceModel_ = deviceModel_;
        if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
          to_bitField0_ |= 0x00000040;
        }
        result.location_ = location_;
        if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
          to_bitField0_ |= 0x00000080;
        }
        result.type_ = type_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof AlertTriggerProtos.AlertTrigger) {
          return mergeFrom((AlertTriggerProtos.AlertTrigger)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(AlertTriggerProtos.AlertTrigger other) {
        if (other == AlertTriggerProtos.AlertTrigger.getDefaultInstance()) return this;
        if (other.hasId()) {
          bitField0_ |= 0x00000001;
          id_ = other.id_;
          onChanged();
        }
        if (other.hasGuid()) {
          bitField0_ |= 0x00000002;
          guid_ = other.guid_;
          onChanged();
        }
        if (other.hasDescription()) {
          bitField0_ |= 0x00000004;
          description_ = other.description_;
          onChanged();
        }
        if (other.hasTenant()) {
          bitField0_ |= 0x00000008;
          tenant_ = other.tenant_;
          onChanged();
        }
        if (other.hasApplication()) {
          bitField0_ |= 0x00000010;
          application_ = other.application_;
          onChanged();
        }
        if (other.hasDeviceModel()) {
          bitField0_ |= 0x00000020;
          deviceModel_ = other.deviceModel_;
          onChanged();
        }
        if (other.hasLocation()) {
          bitField0_ |= 0x00000040;
          location_ = other.location_;
          onChanged();
        }
        if (other.hasType()) {
          bitField0_ |= 0x00000080;
          type_ = other.type_;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasId()) {
          
          return false;
        }
        if (!hasGuid()) {
          
          return false;
        }
        if (!hasTenant()) {
          
          return false;
        }
        if (!hasApplication()) {
          
          return false;
        }
        if (!hasDeviceModel()) {
          
          return false;
        }
        if (!hasLocation()) {
          
          return false;
        }
        if (!hasType()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        AlertTriggerProtos.AlertTrigger parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (AlertTriggerProtos.AlertTrigger) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private java.lang.Object id_ = "";
      /**
       * <code>required string id = 1;</code>
       */
      public boolean hasId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string id = 1;</code>
       */
      public java.lang.String getId() {
        java.lang.Object ref = id_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            id_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string id = 1;</code>
       */
      public com.google.protobuf.ByteString
          getIdBytes() {
        java.lang.Object ref = id_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          id_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string id = 1;</code>
       */
      public Builder setId(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string id = 1;</code>
       */
      public Builder clearId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        id_ = getDefaultInstance().getId();
        onChanged();
        return this;
      }
      /**
       * <code>required string id = 1;</code>
       */
      public Builder setIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object guid_ = "";
      /**
       * <code>required string guid = 2;</code>
       */
      public boolean hasGuid() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required string guid = 2;</code>
       */
      public java.lang.String getGuid() {
        java.lang.Object ref = guid_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            guid_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string guid = 2;</code>
       */
      public com.google.protobuf.ByteString
          getGuidBytes() {
        java.lang.Object ref = guid_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          guid_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string guid = 2;</code>
       */
      public Builder setGuid(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        guid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string guid = 2;</code>
       */
      public Builder clearGuid() {
        bitField0_ = (bitField0_ & ~0x00000002);
        guid_ = getDefaultInstance().getGuid();
        onChanged();
        return this;
      }
      /**
       * <code>required string guid = 2;</code>
       */
      public Builder setGuidBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        guid_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object description_ = "";
      /**
       * <code>optional string description = 3;</code>
       */
      public boolean hasDescription() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>optional string description = 3;</code>
       */
      public java.lang.String getDescription() {
        java.lang.Object ref = description_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            description_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string description = 3;</code>
       */
      public com.google.protobuf.ByteString
          getDescriptionBytes() {
        java.lang.Object ref = description_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          description_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string description = 3;</code>
       */
      public Builder setDescription(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        description_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string description = 3;</code>
       */
      public Builder clearDescription() {
        bitField0_ = (bitField0_ & ~0x00000004);
        description_ = getDefaultInstance().getDescription();
        onChanged();
        return this;
      }
      /**
       * <code>optional string description = 3;</code>
       */
      public Builder setDescriptionBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        description_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object tenant_ = "";
      /**
       * <code>required string tenant = 4;</code>
       */
      public boolean hasTenant() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>required string tenant = 4;</code>
       */
      public java.lang.String getTenant() {
        java.lang.Object ref = tenant_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            tenant_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string tenant = 4;</code>
       */
      public com.google.protobuf.ByteString
          getTenantBytes() {
        java.lang.Object ref = tenant_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          tenant_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string tenant = 4;</code>
       */
      public Builder setTenant(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
        tenant_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string tenant = 4;</code>
       */
      public Builder clearTenant() {
        bitField0_ = (bitField0_ & ~0x00000008);
        tenant_ = getDefaultInstance().getTenant();
        onChanged();
        return this;
      }
      /**
       * <code>required string tenant = 4;</code>
       */
      public Builder setTenantBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
        tenant_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object application_ = "";
      /**
       * <code>required string application = 5;</code>
       */
      public boolean hasApplication() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      /**
       * <code>required string application = 5;</code>
       */
      public java.lang.String getApplication() {
        java.lang.Object ref = application_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            application_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string application = 5;</code>
       */
      public com.google.protobuf.ByteString
          getApplicationBytes() {
        java.lang.Object ref = application_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          application_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string application = 5;</code>
       */
      public Builder setApplication(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000010;
        application_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string application = 5;</code>
       */
      public Builder clearApplication() {
        bitField0_ = (bitField0_ & ~0x00000010);
        application_ = getDefaultInstance().getApplication();
        onChanged();
        return this;
      }
      /**
       * <code>required string application = 5;</code>
       */
      public Builder setApplicationBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000010;
        application_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object deviceModel_ = "";
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public boolean hasDeviceModel() {
        return ((bitField0_ & 0x00000020) == 0x00000020);
      }
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public java.lang.String getDeviceModel() {
        java.lang.Object ref = deviceModel_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            deviceModel_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public com.google.protobuf.ByteString
          getDeviceModelBytes() {
        java.lang.Object ref = deviceModel_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          deviceModel_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public Builder setDeviceModel(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000020;
        deviceModel_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public Builder clearDeviceModel() {
        bitField0_ = (bitField0_ & ~0x00000020);
        deviceModel_ = getDefaultInstance().getDeviceModel();
        onChanged();
        return this;
      }
      /**
       * <code>required string deviceModel = 6;</code>
       */
      public Builder setDeviceModelBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000020;
        deviceModel_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object location_ = "";
      /**
       * <code>required string location = 7;</code>
       */
      public boolean hasLocation() {
        return ((bitField0_ & 0x00000040) == 0x00000040);
      }
      /**
       * <code>required string location = 7;</code>
       */
      public java.lang.String getLocation() {
        java.lang.Object ref = location_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            location_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string location = 7;</code>
       */
      public com.google.protobuf.ByteString
          getLocationBytes() {
        java.lang.Object ref = location_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          location_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string location = 7;</code>
       */
      public Builder setLocation(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000040;
        location_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string location = 7;</code>
       */
      public Builder clearLocation() {
        bitField0_ = (bitField0_ & ~0x00000040);
        location_ = getDefaultInstance().getLocation();
        onChanged();
        return this;
      }
      /**
       * <code>required string location = 7;</code>
       */
      public Builder setLocationBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000040;
        location_ = value;
        onChanged();
        return this;
      }

      private java.lang.Object type_ = "";
      /**
       * <code>required string type = 8;</code>
       */
      public boolean hasType() {
        return ((bitField0_ & 0x00000080) == 0x00000080);
      }
      /**
       * <code>required string type = 8;</code>
       */
      public java.lang.String getType() {
        java.lang.Object ref = type_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            type_ = s;
          }
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>required string type = 8;</code>
       */
      public com.google.protobuf.ByteString
          getTypeBytes() {
        java.lang.Object ref = type_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          type_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string type = 8;</code>
       */
      public Builder setType(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000080;
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string type = 8;</code>
       */
      public Builder clearType() {
        bitField0_ = (bitField0_ & ~0x00000080);
        type_ = getDefaultInstance().getType();
        onChanged();
        return this;
      }
      /**
       * <code>required string type = 8;</code>
       */
      public Builder setTypeBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000080;
        type_ = value;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger)
    }

    static {
      defaultInstance = new AlertTrigger(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:com.konkerlabs.platform.registry.alerts.protobuff.AlertTrigger)
  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\022AlertTrigger.proto\0221com.konkerlabs.pla" +
      "tform.registry.alerts.protobuff\"\227\001\n\014Aler" +
      "tTrigger\022\n\n\002id\030\001 \002(\t\022\014\n\004guid\030\002 \002(\t\022\023\n\013de" +
      "scription\030\003 \001(\t\022\016\n\006tenant\030\004 \002(\t\022\023\n\013appli" +
      "cation\030\005 \002(\t\022\023\n\013deviceModel\030\006 \002(\t\022\020\n\010loc" +
      "ation\030\007 \002(\t\022\014\n\004type\030\010 \002(\tB\026\n\000B\022AlertTrig" +
      "gerProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_konkerlabs_platform_registry_alerts_protobuff_AlertTrigger_descriptor,
        new java.lang.String[] { "Id", "Guid", "Description", "Tenant", "Application", "DeviceModel", "Location", "Type", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
