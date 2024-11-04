package io.henneberger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebMDecoder {

  // EBML Header Elements
  private static final int EBML = 0x1A45DFA3;
  private static final int EBML_VERSION = 0x4286;
  private static final int EBML_READ_VERSION = 0x42F7;
  private static final int EBML_MAX_ID_LENGTH = 0x42F2;
  private static final int EBML_MAX_SIZE_LENGTH = 0x42F3;
  private static final int DOC_TYPE = 0x4282;
  private static final int DOC_TYPE_VERSION = 0x4287;
  private static final int DOC_TYPE_READ_VERSION = 0x4285;
  private static final int DOC_TYPE_EXTENSION = 0x4281;
  private static final int DOC_TYPE_EXTENSION_NAME = 0x4283;
  private static final int DOC_TYPE_EXTENSION_VERSION = 0x4284;

  // Segment Elements
  private static final int SEGMENT = 0x18538067;

  // Top-Level Elements
  private static final int SEEK_HEAD = 0x114D9B74;
  private static final int INFO = 0x1549A966;
  private static final int TRACKS = 0x1654AE6B;
  private static final int CHAPTERS = 0x1043A770;
  private static final int CLUSTER = 0x1F43B675;
  private static final int CUES = 0x1C53BB6B;
  private static final int ATTACHMENTS = 0x1941A469;
  private static final int TAGS = 0x1254C367;

  // SeekHead Elements
  private static final int SEEK = 0x4DBB;
  private static final int SEEK_ID = 0x53AB;
  private static final int SEEK_POSITION = 0x53AC;

  // Info Elements
  private static final int SEGMENT_UUID = 0x73A4;
  private static final int SEGMENT_FILENAME = 0x7384;
  private static final int PREV_UUID = 0x3CB923;
  private static final int PREV_FILENAME = 0x3C83AB;
  private static final int NEXT_UUID = 0x3EB923;
  private static final int NEXT_FILENAME = 0x3E83BB;
  private static final int SEGMENT_FAMILY = 0x4444;
  private static final int CHAPTER_TRANSLATE = 0x6924;
  private static final int TIMESTAMP_SCALE = 0x2AD7B1;
  private static final int DURATION = 0x4489;
  private static final int DATE_UTC = 0x4461;
  private static final int TITLE = 0x7BA9;
  private static final int MUXING_APP = 0x4D80;
  private static final int WRITING_APP = 0x5741;

  // Cluster Elements
  private static final int TIMESTAMP = 0xE7;
  private static final int POSITION = 0xA7;
  private static final int PREV_SIZE = 0xAB;
  private static final int SIMPLE_BLOCK = 0xA3;
  private static final int BLOCK_GROUP = 0xA0;
  private static final int BLOCK = 0xA1;
  private static final int BLOCK_DURATION = 0x9B;
  private static final int REFERENCE_BLOCK = 0xFB;

  // Tracks Elements
  private static final int TRACK_ENTRY = 0xAE;
  private static final int TRACK_NUMBER = 0xD7;
  private static final int TRACK_UID = 0x73C5;
  private static final int TRACK_TYPE = 0x83;
  private static final int FLAG_ENABLED = 0xB9;
  private static final int FLAG_DEFAULT = 0x88;
  private static final int FLAG_FORCED = 0x55AA;
  private static final int FLAG_HEARING_IMPAIRED = 0x55AB;
  private static final int FLAG_VISUAL_IMPAIRED = 0x55AC;
  private static final int FLAG_TEXT_DESCRIPTIONS = 0x55AD;
  private static final int FLAG_ORIGINAL = 0x55AE;
  private static final int FLAG_COMMENTARY = 0x55AF;
  private static final int FLAG_LACING = 0x9C;
  private static final int MIN_CACHE = 0x6DE7;
  private static final int MAX_CACHE = 0x6DF8;
  private static final int DEFAULT_DURATION = 0x23E383;
  private static final int DEFAULT_DECODED_FIELD_DURATION = 0x234E7A;
  private static final int TRACK_TIMESTAMP_SCALE = 0x23314F;
  private static final int TRACK_OFFSET = 0x537F;
  private static final int MAX_BLOCK_ADDITION_ID = 0x55EE;
  private static final int BLOCK_ADDITION_MAPPING = 0x41E4;
  private static final int BLOCK_ADD_ID_VALUE = 0x41F0;
  private static final int BLOCK_ADD_ID_NAME = 0x41A4;
  private static final int BLOCK_ADD_ID_TYPE = 0x41E7;
  private static final int BLOCK_ADD_ID_EXTRA_DATA = 0x41ED;
  private static final int NAME = 0x536E;
  private static final int LANGUAGE = 0x22B59C;
  private static final int LANGUAGE_BCP47 = 0x22B59D;
  private static final int CODEC_ID = 0x86;
  private static final int CODEC_PRIVATE = 0x63A2;
  private static final int CODEC_NAME = 0x258688;
  private static final int ATTACHMENT_LINK = 0x7446;
  private static final int CODEC_SETTINGS = 0x3A9697;
  private static final int CODEC_INFO_URL = 0x3B4040;
  private static final int CODEC_DOWNLOAD_URL = 0x26B240;
  private static final int CODEC_DECODE_ALL = 0xAA;
  private static final int TRACK_OVERLAY = 0x6FAB;
  private static final int CODEC_DELAY = 0x56AA;
  private static final int SEEK_PRE_ROLL = 0x56BB;
  private static final int TRACK_TRANSLATE = 0x6624;
  private static final int TRACK_TRANSLATE_TRACK_ID = 0x66A5;
  private static final int TRACK_TRANSLATE_CODEC = 0x66BF;
  private static final int TRACK_TRANSLATE_EDITION_UID = 0x66FC;
  private static final int VIDEO = 0xE0;
  private static final int AUDIO = 0xE1;
  private static final int TRACK_OPERATION = 0xE2;
  private static final int TRACK_COMBINE_PLANES = 0xE3;
  private static final int TRACK_PLANE = 0xE4;
  private static final int TRACK_PLANE_UID = 0xE5;
  private static final int TRACK_PLANE_TYPE = 0xE6;
  private static final int TRACK_JOIN_BLOCKS = 0xE9;
  private static final int TRACK_JOIN_UID = 0xED;
  private static final int TRICK_TRACK_UID = 0xC0;
  private static final int TRICK_TRACK_SEGMENT_UID = 0xC1;
  private static final int TRICK_TRACK_FLAG = 0xC6;
  private static final int TRICK_MASTER_TRACK_UID = 0xC7;
  private static final int TRICK_MASTER_TRACK_SEGMENT_UID = 0xC4;
  private static final int CONTENT_ENCODINGS = 0x6D80;

  // Video Elements
  private static final int FLAG_INTERLACED = 0x9A;
  private static final int FIELD_ORDER = 0x9D;
  private static final int STEREO_MODE = 0x53B8;
  private static final int ALPHA_MODE = 0x53C0;
  private static final int PIXEL_WIDTH = 0xB0;
  private static final int PIXEL_HEIGHT = 0xBA;
  private static final int PIXEL_CROP_BOTTOM = 0x54AA;
  private static final int PIXEL_CROP_TOP = 0x54BB;
  private static final int PIXEL_CROP_LEFT = 0x54CC;
  private static final int PIXEL_CROP_RIGHT = 0x54DD;
  private static final int DISPLAY_WIDTH = 0x54B0;
  private static final int DISPLAY_HEIGHT = 0x54BA;
  private static final int DISPLAY_UNIT = 0x54B2;
  private static final int ASPECT_RATIO_TYPE = 0x54B3;
  private static final int COLOUR = 0x55B0;
  private static final int GAMMA_VALUE = 0x2FB523;
  private static final int FRAME_RATE = 0x2383E3;
  private static final int UNCOMPRESSED_FOURCC = 0x2EB524;
  private static final int PROJECTION = 0x7670;

  // Colour Elements
  private static final int MATRIX_COEFFICIENTS = 0x55B1;
  private static final int BITS_PER_CHANNEL = 0x55B2;
  private static final int CHROMA_SUBSAMPLING_HORZ = 0x55B3;
  private static final int CHROMA_SUBSAMPLING_VERT = 0x55B4;
  private static final int CB_SUBSAMPLING_HORZ = 0x55B5;
  private static final int CB_SUBSAMPLING_VERT = 0x55B6;
  private static final int CHROMA_SITING_HORZ = 0x55B7;
  private static final int CHROMA_SITING_VERT = 0x55B8;
  private static final int RANGE = 0x55B9;
  private static final int TRANSFER_CHARACTERISTICS = 0x55BA;
  private static final int PRIMARIES = 0x55BB;
  private static final int MAX_CLL = 0x55BC;
  private static final int MAX_FALL = 0x55BD;
  private static final int MASTERING_METADATA = 0x55D0;

  // MasteringMetadata Elements
  private static final int PRIMARY_R_CHROMATICITY_X = 0x55D1;
  private static final int PRIMARY_R_CHROMATICITY_Y = 0x55D2;
  private static final int PRIMARY_G_CHROMATICITY_X = 0x55D3;
  private static final int PRIMARY_G_CHROMATICITY_Y = 0x55D4;
  private static final int PRIMARY_B_CHROMATICITY_X = 0x55D5;
  private static final int PRIMARY_B_CHROMATICITY_Y = 0x55D6;
  private static final int WHITE_POINT_CHROMATICITY_X = 0x55D7;
  private static final int WHITE_POINT_CHROMATICITY_Y = 0x55D8;
  private static final int LUMINANCE_MAX = 0x55D9;
  private static final int LUMINANCE_MIN = 0x55DA;

  // Projection Elements
  private static final int PROJECTION_TYPE = 0x7671;
  private static final int PROJECTION_PRIVATE = 0x7672;
  private static final int PROJECTION_POSE_YAW = 0x7673;
  private static final int PROJECTION_POSE_PITCH = 0x7674;
  private static final int PROJECTION_POSE_ROLL = 0x7675;

  // Audio Elements
  private static final int SAMPLING_FREQUENCY = 0xB5;
  private static final int OUTPUT_SAMPLING_FREQUENCY = 0x78B5;
  private static final int CHANNELS = 0x9F;
  private static final int CHANNEL_POSITIONS = 0x7D7B;
  private static final int BIT_DEPTH = 0x6264;
  private static final int EMPHASIS = 0x52F1;

  // ContentEncodings Elements
  private static final int CONTENT_ENCODING = 0x6240;
  private static final int CONTENT_ENCODING_ORDER = 0x5031;
  private static final int CONTENT_ENCODING_SCOPE = 0x5032;
  private static final int CONTENT_ENCODING_TYPE = 0x5033;
  private static final int CONTENT_COMPRESSION = 0x5034;
  private static final int CONTENT_COMP_ALGO = 0x4254;
  private static final int CONTENT_COMP_SETTINGS = 0x4255;
  private static final int CONTENT_ENCRYPTION = 0x5035;
  private static final int CONTENT_ENC_ALGO = 0x47E1;
  private static final int CONTENT_ENC_KEY_ID = 0x47E2;
  private static final int CONTENT_ENC_AES_SETTINGS = 0x47E7;
  private static final int AES_SETTINGS_CIPHER_MODE = 0x47E8;
  private static final int CONTENT_SIGNATURE = 0x47E3;
  private static final int CONTENT_SIG_KEY_ID = 0x47E4;
  private static final int CONTENT_SIG_ALGO = 0x47E5;
  private static final int CONTENT_SIG_HASH_ALGO = 0x47E6;

  // Cues Elements
  private static final int CUE_POINT = 0xBB;
  private static final int CUE_TIME = 0xB3;
  private static final int CUE_TRACK_POSITIONS = 0xB7;
  private static final int CUE_TRACK = 0xF7;
  private static final int CUE_CLUSTER_POSITION = 0xF1;

  // Attachments Elements
  private static final int ATTACHED_FILE = 0x61A7;
  private static final int FILE_DESCRIPTION = 0x467E;
  private static final int FILE_NAME = 0x466E;
  private static final int FILE_DATA = 0x465C;
  private static final int FILE_UID = 0x46AE;

  // Chapters Elements
  private static final int EDITION_ENTRY = 0x45B9;
  private static final int EDITION_UID = 0x45BC;
  private static final int EDITION_FLAG_DEFAULT = 0x45DB;
  private static final int EDITION_FLAG_ORDERED = 0x45DD;
  private static final int CHAPTER_ATOM = 0xB6;
  private static final int CHAPTER_UID = 0x73C4;
  private static final int CHAPTER_STRING_UID = 0x5654;
  private static final int CHAPTER_TIME_START = 0x91;
  private static final int CHAPTER_TIME_END = 0x92;
  private static final int CHAPTER_FLAG_HIDDEN = 0x98;
  private static final int CHAPTER_DISPLAY = 0x80;
  private static final int CHAP_STRING = 0x85;
  private static final int CHAP_LANGUAGE = 0x437C;

  // Tags Elements
  private static final int TAG = 0x7373;
  private static final int TARGETS = 0x63C0;
  private static final int TARGET_TYPE_VALUE = 0x68CA;
  private static final int TARGET_TYPE = 0x63CA;
  private static final int TAG_TRACK_UID = 0x63C5;
  private static final int TAG_EDITION_UID = 0x63C9;
  private static final int TAG_CHAPTER_UID = 0x63C4;
  private static final int TAG_ATTACHMENT_UID = 0x63C6;
  private static final int SIMPLE_TAG = 0x67C8;
  private static final int TAG_NAME = 0x45A3;
  private static final int TAG_LANGUAGE = 0x447A;
  private static final int TAG_DEFAULT = 0x4484;
  private static final int TAG_STRING = 0x4487;
  private static final int TAG_BINARY = 0x4485;

  // Global Elements
  private static final int CRC_32 = 0xBF;
  private static final int VOID = 0xEC;

  public void decode(InputStream inputStream) throws IOException {
    DataInputStream dis = new DataInputStream(inputStream);

    while (dis.available() > 0) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long size = sizeElement.idOrSize;

      switch ((int) elementId) {
        case EBML:
          System.out.println("Found EBML Header");
          parseEbmlHeader(dis, size);
          break;
        case SEGMENT:
          System.out.println("Found Segment");
          parseSegment(dis, size);
          break;
        default:
          System.out.println("Skipping unknown element: " + Long.toHexString(elementId));
          dis.skipBytes((int) size);
          break;
      }
    }
  }

  private EBMLElement readElementId(DataInputStream dis) throws IOException {
    int firstByte = dis.readUnsignedByte();
    int mask = 0x80;
    int length = 1;
    while ((firstByte & mask) == 0) {
      mask >>= 1;
      length++;
      if (mask == 0) {
        throw new IOException("Invalid EBML ID leading bits");
      }
    }

//        if (length > 4) {
//            throw new IOException("EBML ID length too long: "+ length);
//        }

    long value = firstByte;
    for (int i = 1; i < length; i++) {
      int nextByte = dis.readUnsignedByte();
      value = (value << 8) | nextByte;
    }

    return new EBMLElement(length, value);
  }

  private EBMLElement readElementSize(DataInputStream dis) throws IOException {
    int firstByte = dis.readUnsignedByte();
    int mask = 0x80;
    int length = 1;
    while ((firstByte & mask) == 0) {
      mask >>= 1;
      length++;
      if (mask == 0) {
        throw new IOException("Invalid EBML Size leading bits");
      }
    }

    if (length > 8) {
      throw new IOException("EBML Size length too long");
    }

    long value = firstByte & (mask - 1);

    for (int i = 1; i < length; i++) {
      int nextByte = dis.readUnsignedByte();
      value = (value << 8) | nextByte;
    }

    return new EBMLElement(length, value);
  }

  private void parseEbmlHeader(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case EBML_VERSION:
          int version = dis.readUnsignedByte();
          System.out.println("EBML Version: " + version);
          break;
        case EBML_READ_VERSION:
          int readVersion = dis.readUnsignedByte();
          System.out.println("EBML Read Version: " + readVersion);
          break;
        case EBML_MAX_ID_LENGTH:
          int maxIdLength = dis.readUnsignedByte();
          System.out.println("EBML Max ID Length: " + maxIdLength);
          break;
        case EBML_MAX_SIZE_LENGTH:
          int maxSizeLength = dis.readUnsignedByte();
          System.out.println("EBML Max Size Length: " + maxSizeLength);
          break;
        case DOC_TYPE:
          byte[] docTypeData = new byte[(int) elementSize];
          dis.readFully(docTypeData);
          System.out.println("Doc Type: " + new String(docTypeData, StandardCharsets.UTF_8));
          break;
        case DOC_TYPE_VERSION:
          int docTypeVersion = dis.readUnsignedByte();
          System.out.println("Doc Type Version: " + docTypeVersion);
          break;
        case DOC_TYPE_READ_VERSION:
          int docTypeReadVersion = dis.readUnsignedByte();
          System.out.println("Doc Type Read Version: " + docTypeReadVersion);
          break;
        case DOC_TYPE_EXTENSION:
          System.out.println("Found DocType Extension");
          parseDocTypeExtension(dis, elementSize);
          break;
        default:
          System.out.println(
              "Skipping unknown EBML Header element: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseDocTypeExtension(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case DOC_TYPE_EXTENSION_NAME:
          String extensionName = parseStringElement(dis, (int) elementSize);
          System.out.println("Doc Type Extension Name: " + extensionName);
          break;
        case DOC_TYPE_EXTENSION_VERSION:
          int extensionVersion = dis.readUnsignedByte();
          System.out.println("Doc Type Extension Version: " + extensionVersion);
          break;
        default:
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private String parseStringElement(DataInputStream dis, int size) throws IOException {
    byte[] data = new byte[size];
    dis.readFully(data);

    // Truncate the string at the first Null Octet (0x00)
    int validLength = 0;
    while (validLength < data.length && data[validLength] != 0x00) {
      validLength++;
    }

    return new String(data, 0, validLength, StandardCharsets.UTF_8);
  }

  private void parseSegment(DataInputStream dis, long size) throws IOException {
    long remaining = size;
    while (remaining > 0) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

//            remaining -= idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case SEEK_HEAD:
          System.out.println("Found SeekHead");
          parseSeekHead(dis, elementSize);
          break;
        case INFO:
          System.out.println("Found Info");
          parseSegmentInfo(dis, elementSize);
          break;
        case TRACKS:
          System.out.println("Found Tracks");
          parseTracks(dis, elementSize);
          break;
        case CHAPTERS:
          System.out.println("Found Chapters");
          parseChapters(dis, elementSize);
          break;
        case CLUSTER:
          System.out.println("Found Cluster");
          parseCluster(dis, elementSize);
          break;
        case CUES:
          System.out.println("Found Cues");
          parseCues(dis, elementSize);
          break;
        case ATTACHMENTS:
          System.out.println("Found Attachments");
          parseAttachments(dis, elementSize);
          break;
        case TAGS:
          System.out.println("Found Tags");
          parseTags(dis, elementSize);
          break;
        case CRC_32:
          byte[] crcData = new byte[(int) elementSize];
          dis.readFully(crcData);
          System.out.println("Segment CRC-32 (hex): " + bytesToHex(crcData));
          break;
        case VOID:
          dis.skipBytes((int) elementSize);
          System.out.println("Skipped Void in Segment");
          break;
        default:
          System.out.println("Unknown element in Segment: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseSeekHead(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == SEEK) {
        parseSeek(dis, elementSize);
      } else {
        System.out.println("Unknown element in SeekHead: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseSeek(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == SEEK_ID) {
        byte[] idData = new byte[(int) elementSize];
        dis.readFully(idData);
        System.out.println("SeekID: " + bytesToHex(idData));
      } else if (elementId == SEEK_POSITION) {
        long position = readUnsignedInt(dis, elementSize);
        System.out.println("SeekPosition: " + position);
      } else {
        System.out.println("Unknown element in Seek: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseCluster(DataInputStream dis, long size) throws IOException {
    long remaining = size;
    while (remaining > 0) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      remaining -= idElement.length + sizeElement.length + elementSize;

      if (elementId == TIMESTAMP) {
        long timecode = readUnsignedInt(dis, elementSize);
        System.out.println("Cluster Timecode: " + timecode);
      } else if (elementId == SIMPLE_BLOCK) {
        parseSimpleBlock(dis, elementSize);
      } else if (elementId == BLOCK_GROUP) {
        parseBlockGroup(dis, elementSize);
      } else if (elementId == CLUSTER) {
        System.out.println("Found Cluster");
        remaining = elementSize;
//                dis.skipBytes((int) elementSize);
      } else {
        System.out.println("Skipping:" + elementSize + " " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseSimpleBlock(DataInputStream dis, long size) throws IOException {
    long startPos = dis.available();

    // Read Track Number (EBML VINT)
    EBMLElement trackNumberElement = readElementId(dis);
    long trackNumber = trackNumberElement.idOrSize;
    System.out.println("Track Number: " + trackNumber);

    // Read Timestamp (relative to Cluster timestamp, signed int16)
    short timestamp = dis.readShort();
    System.out.println("Relative Timestamp: " + timestamp);

    // Read Flags (1 byte)
    int flags = dis.readUnsignedByte();

    boolean keyframe = (flags & 0x80) != 0;
    boolean invisible = (flags & 0x08) != 0;
    int lacing = (flags & 0x06) >> 1;
    boolean discardable = (flags & 0x01) != 0;

    System.out.println("Flags:");
    System.out.println("  Keyframe: " + keyframe);
    System.out.println("  Invisible: " + invisible);
    System.out.println("  Lacing: " + lacing);
//        System.out.println("    0 - No lacing");
//        System.out.println("    1 - Xiph lacing");
//        System.out.println("    2 - Fixed-size lacing");
//        System.out.println("    3 - EBML lacing");
    System.out.println("  Discardable: " + discardable);

    // Calculate the remaining size after header
    int headerSize = trackNumberElement.length + 2
        + 1; // TrackNumber length + timestamp (2 bytes) + flags (1 byte)
    long dataSize = size - headerSize;

    if (lacing == 0) {
      // No lacing, read single frame
      byte[] frameData = new byte[(int) dataSize];
      dis.readFully(frameData);
      System.out.println("Read single frame of size: " + frameData.length);
      // Process frameData as needed
    } else {
      // Lacing is used
      int numFrames = dis.readUnsignedByte() + 1;
      System.out.println("Number of frames: " + numFrames);

      int[] frameSizes = new int[numFrames];
      switch (lacing) {
        case 1:
          // Xiph lacing
          for (int i = 0; i < numFrames - 1; i++) {
            int size2 = 0;
            int readByte;
            do {
              readByte = dis.readUnsignedByte();
              size2 += readByte;
            } while (readByte == 255);
            frameSizes[i] = size2;
          }
          // Size of last frame is remaining data
          long bytesRead = 1; // For numFrames byte
          for (int i = 0; i < numFrames - 1; i++) {
            bytesRead += frameSizes[i];
          }
          frameSizes[numFrames - 1] = (int) (dataSize - bytesRead);
          break;

        case 3:
          // EBML lacing
          // Read size of first frame
          EBMLElement frameSizeElement = readElementSize(dis);
          frameSizes[0] = (int) frameSizeElement.idOrSize;
          int previousSize = frameSizes[0];
          for (int i = 1; i < numFrames - 1; i++) {
            // Read difference from previous size
            int sizeDiff = (int) readSignedEBML(dis);
            frameSizes[i] = previousSize + sizeDiff;
            previousSize = frameSizes[i];
          }
          // Size of last frame is remaining data
          long totalSizes = frameSizeElement.length + frameSizes[0];
          for (int i = 1; i < numFrames - 1; i++) {
            totalSizes += frameSizes[i];
          }
          frameSizes[numFrames - 1] = (int) (dataSize - totalSizes);
          break;

        case 2:
          // Fixed-size lacing
          // Calculate size of each frame
          int totalFrameDataSize = (int) (dataSize - 1); // Subtract the numFrames byte
          int frameSize = totalFrameDataSize / numFrames;
          for (int i = 0; i < numFrames; i++) {
            frameSizes[i] = frameSize;
          }
          break;

        default:
          throw new IOException("Unsupported lacing type: " + lacing);
      }

      // Read frames
      for (int i = 0; i < numFrames; i++) {
        byte[] frameData = new byte[frameSizes[i]];
        dis.readFully(frameData);
        System.out.println("Read frame " + (i + 1) + " of size: " + frameSizes[i]);
        // Process frameData as needed
      }
    }

    long endPos = dis.available();
    long bytesConsumed = startPos - endPos;
    if (bytesConsumed != size) {
      System.out.println(
          "Warning: SimpleBlock parsed size mismatch. Expected: " + size + ", Consumed: "
              + bytesConsumed);
      dis.skipBytes((int) (size - bytesConsumed)); // Skip remaining bytes if any
    }
  }

  private long readSignedEBML(DataInputStream dis) throws IOException {
    int firstByte = dis.readUnsignedByte();
    int mask = 0x80;
    int length = 1;
    while ((firstByte & mask) == 0) {
      mask >>= 1;
      length++;
      if (mask == 0) {
        throw new IOException("Invalid EBML VINT leading bits");
      }
    }

    long value = firstByte & (mask - 1);
    for (int i = 1; i < length; i++) {
      int nextByte = dis.readUnsignedByte();
      value = (value << 8) | nextByte;
    }

    // Convert to signed integer
    long maxValue = (1L << (7 * length)) - 1;
    long signedValue = value - ((maxValue + 1) / 2);

    return signedValue;
  }

  private void parseBlockGroup(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      // Handle BlockGroup child elements
      dis.skipBytes((int) elementSize);
    }
  }

  private void parseSegmentInfo(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case SEGMENT_UUID:
          byte[] uuidData = new byte[(int) elementSize];
          dis.readFully(uuidData);
          System.out.println("Segment UUID: " + bytesToHex(uuidData));
          break;
        case SEGMENT_FILENAME:
          String segmentFilename = parseStringElement(dis, (int) elementSize);
          System.out.println("Segment Filename: " + segmentFilename);
          break;
        case PREV_UUID:
          byte[] prevUuidData = new byte[(int) elementSize];
          dis.readFully(prevUuidData);
          System.out.println("Previous Segment UUID: " + bytesToHex(prevUuidData));
          break;
        case PREV_FILENAME:
          String prevFilename = parseStringElement(dis, (int) elementSize);
          System.out.println("Previous Segment Filename: " + prevFilename);
          break;
        case NEXT_UUID:
          byte[] nextUuidData = new byte[(int) elementSize];
          dis.readFully(nextUuidData);
          System.out.println("Next Segment UUID: " + bytesToHex(nextUuidData));
          break;
        case NEXT_FILENAME:
          String nextFilename = parseStringElement(dis, (int) elementSize);
          System.out.println("Next Segment Filename: " + nextFilename);
          break;
        case SEGMENT_FAMILY:
          byte[] segmentFamilyData = new byte[(int) elementSize];
          dis.readFully(segmentFamilyData);
          System.out.println("Segment Family: " + bytesToHex(segmentFamilyData));
          break;
        case CHAPTER_TRANSLATE:
          System.out.println("Found Chapter Translate");
          parseChapterTranslate(dis, elementSize);
          break;
        case TIMESTAMP_SCALE:
          long scale = readUnsignedInt(dis, elementSize);
          System.out.println("Timestamp Scale: " + scale);
          break;
        case DURATION:
          double duration = readFloat(dis, elementSize);
          System.out.println("Duration: " + duration);
          break;
        case DATE_UTC:
          long dateUtc = readDate(dis, elementSize);
          System.out.println("Date UTC: " + new java.util.Date(dateUtc));
          break;
        case TITLE:
          String title = parseStringElement(dis, (int) elementSize);
          System.out.println("Title: " + title);
          break;
        case MUXING_APP:
          String muxingApp = parseStringElement(dis, (int) elementSize);
          System.out.println("Muxing App: " + muxingApp);
          break;
        case WRITING_APP:
          String writingApp = parseStringElement(dis, (int) elementSize);
          System.out.println("Writing App: " + writingApp);
          break;
        default:
          System.out.println("Unknown element in Info: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseChapterTranslate(DataInputStream dis, long size) throws IOException {
    // Implement parsing of ChapterTranslate child elements as needed
    dis.skipBytes((int) size); // Placeholder
  }

  private long readDate(DataInputStream dis, long size) throws IOException {
    if (size != 8) {
      throw new IOException("Invalid date size: " + size);
    }
    long date = dis.readLong();
    // Convert from nanoseconds since 2001-01-01T00:00:00 UTC to milliseconds since 1970-01-01T00:00:00 UTC
    long dateUtc = date / 1000000L + 978307200000L;
    return dateUtc;
  }

  private void parseTracks(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == TRACK_ENTRY) {
        System.out.println("Found Track Entry");
        parseTrackEntry(dis, elementSize);
      } else {
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseTrackEntry(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case TRACK_NUMBER:
          long trackNumber = readUnsignedInt(dis, elementSize);
          System.out.println("Track Number: " + trackNumber);
          break;
        case TRACK_UID:
          long trackUID = readUnsignedInt(dis, elementSize);
          System.out.println("Track UID: " + trackUID);
          break;
        case TRACK_TYPE:
          int trackType = dis.readUnsignedByte();
          System.out.println("Track Type: " + trackType);
//                    System.out.println("    1 - video");
//                    System.out.println("    2 - audio");
//                    System.out.println("    3 - complex");
//                    System.out.println("    16 - logo");
//                    System.out.println("    17 - subtitle");
//                    System.out.println("    18 - buttons");
//                    System.out.println("    32 - control");
//                    System.out.println("    33 - metadata");
          break;
        case FLAG_ENABLED:
          int flagEnabled = dis.readUnsignedByte();
          System.out.println("Flag Enabled: " + flagEnabled);
          break;
        case FLAG_DEFAULT:
          int flagDefault = dis.readUnsignedByte();
          System.out.println("Flag Default: " + flagDefault);
          break;
        case FLAG_FORCED:
          int flagForced = dis.readUnsignedByte();
          System.out.println("Flag Forced: " + flagForced);
          break;
        case FLAG_HEARING_IMPAIRED:
          int flagHearingImpaired = dis.readUnsignedByte();
          System.out.println("Flag Hearing Impaired: " + flagHearingImpaired);
          break;
        case FLAG_VISUAL_IMPAIRED:
          int flagVisualImpaired = dis.readUnsignedByte();
          System.out.println("Flag Visual Impaired: " + flagVisualImpaired);
          break;
        case FLAG_TEXT_DESCRIPTIONS:
          int flagTextDescriptions = dis.readUnsignedByte();
          System.out.println("Flag Text Descriptions: " + flagTextDescriptions);
          break;
        case FLAG_ORIGINAL:
          int flagOriginal = dis.readUnsignedByte();
          System.out.println("Flag Original: " + flagOriginal);
          break;
        case FLAG_COMMENTARY:
          int flagCommentary = dis.readUnsignedByte();
          System.out.println("Flag Commentary: " + flagCommentary);
          break;
        case FLAG_LACING:
          int flagLacing = dis.readUnsignedByte();
          System.out.println("Flag Lacing: " + flagLacing);
          break;
        case MIN_CACHE:
          long minCache = readUnsignedInt(dis, elementSize);
          System.out.println("Min Cache: " + minCache);
          break;
        case MAX_CACHE:
          long maxCache = readUnsignedInt(dis, elementSize);
          System.out.println("Max Cache: " + maxCache);
          break;
        case DEFAULT_DURATION:
          long defaultDuration = readUnsignedInt(dis, elementSize);
          System.out.println("Default Duration: " + defaultDuration);
          break;
        case DEFAULT_DECODED_FIELD_DURATION:
          long defaultDecodedFieldDuration = readUnsignedInt(dis, elementSize);
          System.out.println("Default Decoded Field Duration: " + defaultDecodedFieldDuration);
          break;
        case TRACK_TIMESTAMP_SCALE:
          double trackTimestampScale = readFloat(dis, elementSize);
          System.out.println("Track Timestamp Scale: " + trackTimestampScale);
          break;
        case TRACK_OFFSET:
          long trackOffset = readSignedInt(dis, elementSize);
          System.out.println("Track Offset: " + trackOffset);
          break;
        case MAX_BLOCK_ADDITION_ID:
          long maxBlockAdditionID = readUnsignedInt(dis, elementSize);
          System.out.println("Max Block Addition ID: " + maxBlockAdditionID);
          break;
        case BLOCK_ADDITION_MAPPING:
          System.out.println("Found Block Addition Mapping");
          parseBlockAdditionMapping(dis, elementSize);
          break;
        case NAME:
          String name = parseStringElement(dis, (int) elementSize);
          System.out.println("Name: " + name);
          break;
        case LANGUAGE:
          String language = parseStringElement(dis, (int) elementSize);
          System.out.println("Language: " + language);
          break;
        case LANGUAGE_BCP47:
          String languageBCP47 = parseStringElement(dis, (int) elementSize);
          System.out.println("Language BCP47: " + languageBCP47);
          break;
        case CODEC_ID:
          String codecID = parseStringElement(dis, (int) elementSize);
          System.out.println("Codec ID: " + codecID);
          break;
        case CODEC_PRIVATE:
          byte[] codecPrivateData = new byte[(int) elementSize];
          dis.readFully(codecPrivateData);
          System.out.println("Codec Private Data: " + bytesToHex(codecPrivateData));
          break;
        case CODEC_NAME:
          String codecName = parseStringElement(dis, (int) elementSize);
          System.out.println("Codec Name: " + codecName);
          break;
        case ATTACHMENT_LINK:
          long attachmentLink = readUnsignedInt(dis, elementSize);
          System.out.println("Attachment Link: " + attachmentLink);
          break;
        case CODEC_SETTINGS:
          String codecSettings = parseStringElement(dis, (int) elementSize);
          System.out.println("Codec Settings: " + codecSettings);
          break;
        case CODEC_INFO_URL:
          String codecInfoURL = parseStringElement(dis, (int) elementSize);
          System.out.println("Codec Info URL: " + codecInfoURL);
          break;
        case CODEC_DOWNLOAD_URL:
          String codecDownloadURL = parseStringElement(dis, (int) elementSize);
          System.out.println("Codec Download URL: " + codecDownloadURL);
          break;
        case CODEC_DECODE_ALL:
          int codecDecodeAll = dis.readUnsignedByte();
          System.out.println("Codec Decode All: " + codecDecodeAll);
          break;
        case TRACK_OVERLAY:
          long trackOverlay = readUnsignedInt(dis, elementSize);
          System.out.println("Track Overlay: " + trackOverlay);
          break;
        case CODEC_DELAY:
          long codecDelay = readUnsignedInt(dis, elementSize);
          System.out.println("Codec Delay: " + codecDelay);
          break;
        case SEEK_PRE_ROLL:
          long seekPreRoll = readUnsignedInt(dis, elementSize);
          System.out.println("Seek Pre Roll: " + seekPreRoll);
          break;
        case TRACK_TRANSLATE:
          System.out.println("Found Track Translate");
          parseTrackTranslate(dis, elementSize);
          break;
        case VIDEO:
          System.out.println("Found Video Track");
          parseVideo(dis, elementSize);
          break;
        case AUDIO:
          System.out.println("Found Audio Track");
          parseAudio(dis, elementSize);
          break;
        case CONTENT_ENCODINGS:
          System.out.println("Found Content Encodings");
          parseContentEncodings(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Track Entry: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseVideo(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case FLAG_INTERLACED:
          int flagInterlaced = dis.readUnsignedByte();
          System.out.println("Flag Interlaced: " + flagInterlaced);
          break;
        case FIELD_ORDER:
          int fieldOrder = dis.readUnsignedByte();
          System.out.println("Field Order: " + fieldOrder);
          break;
        case STEREO_MODE:
          int stereoMode = dis.readUnsignedByte();
          System.out.println("Stereo Mode: " + stereoMode);
          break;
        case ALPHA_MODE:
          int alphaMode = dis.readUnsignedByte();
          System.out.println("Alpha Mode: " + alphaMode);
          break;
        case PIXEL_WIDTH:
          long pixelWidth = readUnsignedInt(dis, elementSize);
          System.out.println("Pixel Width: " + pixelWidth);
          break;
        case PIXEL_HEIGHT:
          long pixelHeight = readUnsignedInt(dis, elementSize);
          System.out.println("Pixel Height: " + pixelHeight);
          break;
        case DISPLAY_WIDTH:
          long displayWidth = readUnsignedInt(dis, elementSize);
          System.out.println("Display Width: " + displayWidth);
          break;
        case DISPLAY_HEIGHT:
          long displayHeight = readUnsignedInt(dis, elementSize);
          System.out.println("Display Height: " + displayHeight);
          break;
        case ASPECT_RATIO_TYPE:
          int aspectRatioType = dis.readUnsignedByte();
          System.out.println("Aspect Ratio Type: " + aspectRatioType);
          break;
        case COLOUR:
          System.out.println("Found Colour");
          parseColor(dis, elementSize);
          break;
        case PROJECTION:
          System.out.println("Found Projection");
          parseProjection(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Video: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseColor(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case MATRIX_COEFFICIENTS:
          int matrixCoefficients = dis.readUnsignedByte();
          System.out.println("Matrix Coefficients: " + matrixCoefficients);
          break;
        case BITS_PER_CHANNEL:
          int bitsPerChannel = dis.readUnsignedByte();
          System.out.println("Bits Per Channel: " + bitsPerChannel);
          break;
        case CHROMA_SUBSAMPLING_HORZ:
          int chromaSubsamplingHorz = dis.readUnsignedByte();
          System.out.println("Chroma Subsampling Horizontal: " + chromaSubsamplingHorz);
          break;
        case CHROMA_SUBSAMPLING_VERT:
          int chromaSubsamplingVert = dis.readUnsignedByte();
          System.out.println("Chroma Subsampling Vertical: " + chromaSubsamplingVert);
          break;
        case RANGE:
          int range = dis.readUnsignedByte();
          System.out.println("Range: " + range);
          break;
        case TRANSFER_CHARACTERISTICS:
          int transferCharacteristics = dis.readUnsignedByte();
          System.out.println("Transfer Characteristics: " + transferCharacteristics);
          break;
        case PRIMARIES:
          int primaries = dis.readUnsignedByte();
          System.out.println("Primaries: " + primaries);
          break;
        case MAX_CLL:
          int maxCLL = dis.readInt();
          System.out.println("Max Content Light Level (CLL): " + maxCLL);
          break;
        case MAX_FALL:
          int maxFALL = dis.readInt();
          System.out.println("Max Frame-Average Light Level (FALL): " + maxFALL);
          break;
        default:
          System.out.println("Unknown element in Colour: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseProjection(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case PROJECTION_TYPE:
          int projectionType = dis.readUnsignedByte();
          System.out.println("Projection Type: " + projectionType);
          break;
        case PROJECTION_PRIVATE:
          byte[] projectionPrivateData = new byte[(int) elementSize];
          dis.readFully(projectionPrivateData);
          System.out.println("Projection Private Data: " + bytesToHex(projectionPrivateData));
          break;
        case PROJECTION_POSE_YAW:
          double projectionPoseYaw = readFloat(dis, elementSize);
          System.out.println("Projection Pose Yaw: " + projectionPoseYaw);
          break;
        case PROJECTION_POSE_PITCH:
          double projectionPosePitch = readFloat(dis, elementSize);
          System.out.println("Projection Pose Pitch: " + projectionPosePitch);
          break;
        case PROJECTION_POSE_ROLL:
          double projectionPoseRoll = readFloat(dis, elementSize);
          System.out.println("Projection Pose Roll: " + projectionPoseRoll);
          break;
        default:
          System.out.println("Unknown element in Projection: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseAudio(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case SAMPLING_FREQUENCY:
          double samplingFrequency = readFloat(dis, elementSize);
          System.out.println("Sampling Frequency: " + samplingFrequency);
          break;
        case OUTPUT_SAMPLING_FREQUENCY:
          double outputSamplingFrequency = readFloat(dis, elementSize);
          System.out.println("Output Sampling Frequency: " + outputSamplingFrequency);
          break;
        case CHANNELS:
          long channels = readUnsignedInt(dis, elementSize);
          System.out.println("Channels: " + channels);
          break;
        case BIT_DEPTH:
          long bitDepth = readUnsignedInt(dis, elementSize);
          System.out.println("Bit Depth: " + bitDepth);
          break;
        default:
          System.out.println("Unknown element in Audio: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseBlockAdditionMapping(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case BLOCK_ADD_ID_VALUE:
          long blockAddIDValue = readUnsignedInt(dis, elementSize);
          System.out.println("Block Add ID Value: " + blockAddIDValue);
          break;
        case BLOCK_ADD_ID_NAME:
          String blockAddIDName = parseStringElement(dis, (int) elementSize);
          System.out.println("Block Add ID Name: " + blockAddIDName);
          break;
        case BLOCK_ADD_ID_TYPE:
          long blockAddIDType = readUnsignedInt(dis, elementSize);
          System.out.println("Block Add ID Type: " + blockAddIDType);
          break;
        case BLOCK_ADD_ID_EXTRA_DATA:
          byte[] blockAddIDExtraData = new byte[(int) elementSize];
          dis.readFully(blockAddIDExtraData);
          System.out.println("Block Add ID Extra Data: " + bytesToHex(blockAddIDExtraData));
          break;
        default:
          System.out.println(
              "Unknown element in Block Addition Mapping: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseTrackTranslate(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case TRACK_TRANSLATE_TRACK_ID:
          byte[] trackTranslateTrackID = new byte[(int) elementSize];
          dis.readFully(trackTranslateTrackID);
          System.out.println("Track Translate Track ID: " + bytesToHex(trackTranslateTrackID));
          break;
        case TRACK_TRANSLATE_CODEC:
          long trackTranslateCodec = readUnsignedInt(dis, elementSize);
          System.out.println("Track Translate Codec: " + trackTranslateCodec);
          break;
        case TRACK_TRANSLATE_EDITION_UID:
          long trackTranslateEditionUID = readUnsignedInt(dis, elementSize);
          System.out.println("Track Translate Edition UID: " + trackTranslateEditionUID);
          break;
        default:
          System.out.println("Unknown element in Track Translate: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseContentEncodings(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == CONTENT_ENCODING) {
        System.out.println("Found Content Encoding");
        parseContentEncoding(dis, elementSize);
      } else {
        System.out.println("Unknown element in Content Encodings: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseContentEncoding(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CONTENT_ENCODING_ORDER:
          long contentEncodingOrder = readUnsignedInt(dis, elementSize);
          System.out.println("Content Encoding Order: " + contentEncodingOrder);
          break;
        case CONTENT_ENCODING_SCOPE:
          long contentEncodingScope = readUnsignedInt(dis, elementSize);
          System.out.println("Content Encoding Scope: " + contentEncodingScope);
          System.out.println("    1 - All frame contents");
          System.out.println("    2 - The track's private data");
          System.out.println("    4 - Next ContentEncoding (deprecated)");
          break;
        case CONTENT_ENCODING_TYPE:
          long contentEncodingType = readUnsignedInt(dis, elementSize);
          System.out.println("Content Encoding Type: " + contentEncodingType);
          System.out.println("    0 - Compression");
          System.out.println("    1 - Encryption");
          break;
        case CONTENT_COMPRESSION:
          System.out.println("Found Content Compression");
          parseContentCompression(dis, elementSize);
          break;
        case CONTENT_ENCRYPTION:
          System.out.println("Found Content Encryption");
          parseContentEncryption(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Content Encoding: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseContentCompression(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CONTENT_COMP_ALGO:
          long contentCompAlgo = readUnsignedInt(dis, elementSize);
          System.out.println("Content Compression Algorithm: " + contentCompAlgo);
          System.out.println("    0 - zlib");
          System.out.println("    1 - bzlib");
          System.out.println("    2 - lzo1x");
          System.out.println("    3 - Header Stripping");
          break;
        case CONTENT_COMP_SETTINGS:
          byte[] contentCompSettings = new byte[(int) elementSize];
          dis.readFully(contentCompSettings);
          System.out.println("Content Compression Settings: " + bytesToHex(contentCompSettings));
          break;
        default:
          System.out.println(
              "Unknown element in Content Compression: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseContentEncryption(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CONTENT_ENC_ALGO:
          long contentEncAlgo = readUnsignedInt(dis, elementSize);
          System.out.println("Content Encryption Algorithm: " + contentEncAlgo);
          System.out.println("    0 - Not encrypted");
          System.out.println("    1 - DES");
          System.out.println("    2 - 3DES");
          System.out.println("    3 - Twofish");
          System.out.println("    4 - Blowfish");
          System.out.println("    5 - AES");
          break;
        case CONTENT_ENC_KEY_ID:
          byte[] contentEncKeyID = new byte[(int) elementSize];
          dis.readFully(contentEncKeyID);
          System.out.println("Content Encryption Key ID: " + bytesToHex(contentEncKeyID));
          break;
        case CONTENT_ENC_AES_SETTINGS:
          System.out.println("Found Content Encryption AES Settings");
          parseContentEncAESSettings(dis, elementSize);
          break;
        default:
          System.out.println(
              "Unknown element in Content Encryption: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseContentEncAESSettings(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == AES_SETTINGS_CIPHER_MODE) {
        long aesCipherMode = readUnsignedInt(dis, elementSize);
        System.out.println("AES Settings Cipher Mode: " + aesCipherMode);
        System.out.println("    1 - AES-CTR");
        System.out.println("    2 - AES-CBC");
      } else {
        System.out.println(
            "Unknown element in Content Enc AES Settings: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseChapters(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == EDITION_ENTRY) {
        System.out.println("Found Edition Entry");
        parseEditionEntry(dis, elementSize);
      } else {
        System.out.println("Unknown element in Chapters: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseEditionEntry(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case EDITION_UID:
          long editionUID = readUnsignedInt(dis, elementSize);
          System.out.println("Edition UID: " + editionUID);
          break;
        case EDITION_FLAG_DEFAULT:
          int flagDefault = dis.readUnsignedByte();
          System.out.println("Edition Flag Default: " + flagDefault);
          break;
        case EDITION_FLAG_ORDERED:
          int flagOrdered = dis.readUnsignedByte();
          System.out.println("Edition Flag Ordered: " + flagOrdered);
          break;
        case CHAPTER_ATOM:
          System.out.println("Found Chapter Atom");
          parseChapterAtom(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Edition Entry: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseChapterAtom(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CHAPTER_UID:
          long chapterUID = readUnsignedInt(dis, elementSize);
          System.out.println("Chapter UID: " + chapterUID);
          break;
        case CHAPTER_STRING_UID:
          String chapterStringUID = parseStringElement(dis, (int) elementSize);
          System.out.println("Chapter String UID: " + chapterStringUID);
          break;
        case CHAPTER_TIME_START:
          long chapterTimeStart = readUnsignedInt(dis, elementSize);
          System.out.println("Chapter Time Start: " + chapterTimeStart);
          break;
        case CHAPTER_TIME_END:
          long chapterTimeEnd = readUnsignedInt(dis, elementSize);
          System.out.println("Chapter Time End: " + chapterTimeEnd);
          break;
        case CHAPTER_FLAG_HIDDEN:
          int chapterFlagHidden = dis.readUnsignedByte();
          System.out.println("Chapter Flag Hidden: " + chapterFlagHidden);
          break;
        case CHAPTER_DISPLAY:
          System.out.println("Found Chapter Display");
          parseChapterDisplay(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Chapter Atom: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseChapterDisplay(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CHAP_STRING:
          String chapString = parseStringElement(dis, (int) elementSize);
          System.out.println("ChapString: " + chapString);
          break;
        case CHAP_LANGUAGE:
          String chapLanguage = parseStringElement(dis, (int) elementSize);
          System.out.println("ChapLanguage: " + chapLanguage);
          break;
        default:
          System.out.println("Unknown element in Chapter Display: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseCues(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == CUE_POINT) {
        System.out.println("Found Cue Point");
        parseCuePoint(dis, elementSize);
      } else {
        System.out.println("Unknown element in Cues: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseCuePoint(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CUE_TIME:
          long cueTime = readUnsignedInt(dis, elementSize);
          System.out.println("Cue Time: " + cueTime);
          break;
        case CUE_TRACK_POSITIONS:
          System.out.println("Found Cue Track Positions");
          parseCueTrackPositions(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Cue Point: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseCueTrackPositions(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case CUE_TRACK:
          long cueTrack = readUnsignedInt(dis, elementSize);
          System.out.println("Cue Track: " + cueTrack);
          break;
        case CUE_CLUSTER_POSITION:
          long cueClusterPosition = readUnsignedInt(dis, elementSize);
          System.out.println("Cue Cluster Position: " + cueClusterPosition);
          break;
        default:
          System.out.println(
              "Unknown element in Cue Track Positions: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseAttachments(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == ATTACHED_FILE) {
        System.out.println("Found Attached File");
        parseAttachedFile(dis, elementSize);
      } else {
        System.out.println("Unknown element in Attachments: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseAttachedFile(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case FILE_DESCRIPTION:
          String fileDescription = parseStringElement(dis, (int) elementSize);
          System.out.println("File Description: " + fileDescription);
          break;
        case FILE_NAME:
          String fileName = parseStringElement(dis, (int) elementSize);
          System.out.println("File Name: " + fileName);
          break;
        case FILE_DATA:
          byte[] fileData = new byte[(int) elementSize];
          dis.readFully(fileData);
          System.out.println("File Data: " + bytesToHex(fileData));
          break;
        case FILE_UID:
          long fileUID = readUnsignedInt(dis, elementSize);
          System.out.println("File UID: " + fileUID);
          break;
        default:
          System.out.println("Unknown element in Attached File: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseTags(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      if (elementId == TAG) {
        System.out.println("Found Tag");
        parseTag(dis, elementSize);
      } else {
        System.out.println("Unknown element in Tags: " + Long.toHexString(elementId));
        dis.skipBytes((int) elementSize);
      }
    }
  }

  private void parseTag(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case TARGETS:
          System.out.println("Found Targets");
          parseTargets(dis, elementSize);
          break;
        case SIMPLE_TAG:
          System.out.println("Found SimpleTag");
          parseSimpleTag(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in Tag: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseTargets(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case TARGET_TYPE_VALUE:
          long targetTypeValue = readUnsignedInt(dis, elementSize);
          System.out.println("Target Type Value: " + targetTypeValue);
          break;
        case TARGET_TYPE:
          String targetType = parseStringElement(dis, (int) elementSize);
          System.out.println("Target Type: " + targetType);
          break;
        case TAG_TRACK_UID:
          long tagTrackUID = readUnsignedInt(dis, elementSize);
          System.out.println("Tag Track UID: " + tagTrackUID);
          break;
        case TAG_EDITION_UID:
          long tagEditionUID = readUnsignedInt(dis, elementSize);
          System.out.println("Tag Edition UID: " + tagEditionUID);
          break;
        case TAG_CHAPTER_UID:
          long tagChapterUID = readUnsignedInt(dis, elementSize);
          System.out.println("Tag Chapter UID: " + tagChapterUID);
          break;
        case TAG_ATTACHMENT_UID:
          long tagAttachmentUID = readUnsignedInt(dis, elementSize);
          System.out.println("Tag Attachment UID: " + tagAttachmentUID);
          break;
        default:
          System.out.println("Unknown element in Targets: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private void parseSimpleTag(DataInputStream dis, long size) throws IOException {
    long bytesRead = 0;
    while (bytesRead < size) {
      EBMLElement idElement = readElementId(dis);
      EBMLElement sizeElement = readElementSize(dis);

      long elementId = idElement.idOrSize;
      long elementSize = sizeElement.idOrSize;

      bytesRead += idElement.length + sizeElement.length + elementSize;

      switch ((int) elementId) {
        case TAG_NAME:
          String tagName = parseStringElement(dis, (int) elementSize);
          System.out.println("Tag Name: " + tagName);
          break;
        case TAG_LANGUAGE:
          String tagLanguage = parseStringElement(dis, (int) elementSize);
          System.out.println("Tag Language: " + tagLanguage);
          break;
        case TAG_DEFAULT:
          int tagDefault = dis.readUnsignedByte();
          System.out.println("Tag Default: " + tagDefault);
          break;
        case TAG_STRING:
          String tagString = parseStringElement(dis, (int) elementSize);
          System.out.println("Tag String: " + tagString);
          break;
        case TAG_BINARY:
          byte[] tagBinary = new byte[(int) elementSize];
          dis.readFully(tagBinary);
          System.out.println("Tag Binary: " + bytesToHex(tagBinary));
          break;
        case SIMPLE_TAG:
          System.out.println("Found Nested SimpleTag");
          parseSimpleTag(dis, elementSize);
          break;
        default:
          System.out.println("Unknown element in SimpleTag: " + Long.toHexString(elementId));
          dis.skipBytes((int) elementSize);
          break;
      }
    }
  }

  private long readSignedInt(DataInputStream dis, long size) throws IOException {
    long value = 0;
    for (int i = 0; i < size; i++) {
      int b = dis.readUnsignedByte();
      value = (value << 8) | b;
    }
    // Convert to signed value
    long maxValue = 1L << (size * 8 - 1);
    if (value >= maxValue) {
      value -= 2 * maxValue;
    }
    return value;
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString();
  }

  private long readUnsignedInt(DataInputStream dis, long size) throws IOException {
    long value = 0;
    for (int i = 0; i < size; i++) {
      int b = dis.readUnsignedByte();
      value = (value << 8) | b;
    }
    return value;
  }

  private double readFloat(DataInputStream dis, long size) throws IOException {
    if (size == 4) {
      return dis.readFloat();
    } else if (size == 8) {
      return dis.readDouble();
    } else {
      throw new IOException("Invalid float size: " + size);
    }
  }

  public static void main(String[] args) throws IOException {
    byte[] webmData = Files.readAllBytes(
        Path.of("/Users/henneberger/multipart/stream_output.webm"));
    WebMDecoder decoder = new WebMDecoder();

    try (InputStream inputStream = new ByteArrayInputStream(webmData)) {
      decoder.decode(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Helper Classes and Methods
  class EBMLElement {

    public int length;
    public long idOrSize;

    public EBMLElement(int length, long idOrSize) {
      this.length = length;
      this.idOrSize = idOrSize;
    }
  }
}
