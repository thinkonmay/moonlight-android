package com.limelight.nvstream;

import com.limelight.nvstream.jni.MoonBridge;

public class StreamConfiguration {
    private int width, height;
    private int refreshRate;
    private int launchRefreshRate;
    private int clientRefreshRateX100;
    private int bitrate;

    private MoonBridge.AudioConfiguration audioConfiguration;
    private int supportedVideoFormats;
    private int attachedGamepadMask;
    private int colorRange;
    private int colorSpace;

    public static class Builder {
        private StreamConfiguration config = new StreamConfiguration();
        
        public StreamConfiguration.Builder setResolution(int width, int height) {
            config.width = width;
            config.height = height;
            return this;
        }
        
        public StreamConfiguration.Builder setRefreshRate(int refreshRate) {
            config.refreshRate = refreshRate;
            return this;
        }

        public StreamConfiguration.Builder setLaunchRefreshRate(int refreshRate) {
            config.launchRefreshRate = refreshRate;
            return this;
        }
        
        public StreamConfiguration.Builder setBitrate(int bitrate) {
            config.bitrate = bitrate;
            return this;
        }
        
        public StreamConfiguration.Builder setAttachedGamepadMask(int attachedGamepadMask) {
            config.attachedGamepadMask = attachedGamepadMask;
            return this;
        }

        public StreamConfiguration.Builder setAttachedGamepadMaskByCount(int gamepadCount) {
            config.attachedGamepadMask = 0;
            for (int i = 0; i < 4; i++) {
                if (gamepadCount > i) {
                    config.attachedGamepadMask |= 1 << i;
                }
            }
            return this;
        }

        public StreamConfiguration.Builder setClientRefreshRateX100(int refreshRateX100) {
            config.clientRefreshRateX100 = refreshRateX100;
            return this;
        }

        public StreamConfiguration.Builder setAudioConfiguration(MoonBridge.AudioConfiguration audioConfig) {
            config.audioConfiguration = audioConfig;
            return this;
        }
        
        public StreamConfiguration.Builder setSupportedVideoFormats(int supportedVideoFormats) {
            config.supportedVideoFormats = supportedVideoFormats;
            return this;
        }

        public StreamConfiguration.Builder setColorRange(int colorRange) {
            config.colorRange = colorRange;
            return this;
        }

        public StreamConfiguration.Builder setColorSpace(int colorSpace) {
            config.colorSpace = colorSpace;
            return this;
        }

        public StreamConfiguration build() {
            return config;
        }
    }
    
    private StreamConfiguration() {
        // Set default attributes
        this.width = 1280;
        this.height = 720;
        this.refreshRate = 60;
        this.launchRefreshRate = 60;
        this.bitrate = 10000;
        this.audioConfiguration = MoonBridge.AUDIO_CONFIGURATION_STEREO;
        this.supportedVideoFormats = MoonBridge.VIDEO_FORMAT_H264;
        this.attachedGamepadMask = 0;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getRefreshRate() {
        return refreshRate;
    }

    public int getLaunchRefreshRate() {
        return launchRefreshRate;
    }
    
    public int getBitrate() {
        return bitrate;
    }
    

    public MoonBridge.AudioConfiguration getAudioConfiguration() {
        return audioConfiguration;
    }
    
    public int getSupportedVideoFormats() {
        return supportedVideoFormats;
    }

    public int getAttachedGamepadMask() {
        return attachedGamepadMask;
    }

    public int getClientRefreshRateX100() {
        return clientRefreshRateX100;
    }

    public int getColorRange() {
        return colorRange;
    }

    public int getColorSpace() {
        return colorSpace;
    }
}
