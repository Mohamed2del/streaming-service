package javaservices.streamingservice.models;

public class StreamInfo  {
    int index;
    String codecType;
    String codecName;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getCodecType() {
        return codecType;
    }

    public void setCodecType(String codecType) {
        this.codecType = codecType;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    String language;

    public StreamInfo(int index, String codecType, String codecName, String language) {
        this.index = index;
        this.codecType = codecType;
        this.codecName = codecName;
        this.language = language;
    }

}
