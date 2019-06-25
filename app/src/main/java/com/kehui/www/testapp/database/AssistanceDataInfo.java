package com.kehui.www.testapp.database;

import org.greenrobot.greendao.annotation.Entity;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by jwj on 2018/4/11.
 */
@Entity
public class AssistanceDataInfo {
    @Id(autoincrement = true)
    private Long id;
    private String InfoId;
    private long testTime;
    private String testName;
    private String testPosition;
    private String cableLength;
    private String cableType;
    private String faultType;
    private String faultLength;
    private String shortNote;
    private String dataCollection;
    private String reportStatus;
    private String replyStatus;//1   0
    private String replyContent;//1   0
    private int magneticFieldGain;
    private int VoiceGain;
    private int filterMode;
    private String language;
    public String getLanguage() {
        return this.language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public int getFilterMode() {
        return this.filterMode;
    }
    public void setFilterMode(int filterMode) {
        this.filterMode = filterMode;
    }
    public int getVoiceGain() {
        return this.VoiceGain;
    }
    public void setVoiceGain(int VoiceGain) {
        this.VoiceGain = VoiceGain;
    }
    public int getMagneticFieldGain() {
        return this.magneticFieldGain;
    }
    public void setMagneticFieldGain(int magneticFieldGain) {
        this.magneticFieldGain = magneticFieldGain;
    }
    public String getReplyContent() {
        return this.replyContent;
    }
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
    public String getReplyStatus() {
        return this.replyStatus;
    }
    public void setReplyStatus(String replyStatus) {
        this.replyStatus = replyStatus;
    }
    public String getReportStatus() {
        return this.reportStatus;
    }
    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }
    public String getDataCollection() {
        return this.dataCollection;
    }
    public void setDataCollection(String dataCollection) {
        this.dataCollection = dataCollection;
    }
    public String getShortNote() {
        return this.shortNote;
    }
    public void setShortNote(String shortNote) {
        this.shortNote = shortNote;
    }
    public String getFaultLength() {
        return this.faultLength;
    }
    public void setFaultLength(String faultLength) {
        this.faultLength = faultLength;
    }
    public String getFaultType() {
        return this.faultType;
    }
    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }
    public String getCableType() {
        return this.cableType;
    }
    public void setCableType(String cableType) {
        this.cableType = cableType;
    }
    public String getCableLength() {
        return this.cableLength;
    }
    public void setCableLength(String cableLength) {
        this.cableLength = cableLength;
    }
    public String getTestPosition() {
        return this.testPosition;
    }
    public void setTestPosition(String testPosition) {
        this.testPosition = testPosition;
    }
    public String getTestName() {
        return this.testName;
    }
    public void setTestName(String testName) {
        this.testName = testName;
    }
    public long getTestTime() {
        return this.testTime;
    }
    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }
    public String getInfoId() {
        return this.InfoId;
    }
    public void setInfoId(String InfoId) {
        this.InfoId = InfoId;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1688679213)
    public AssistanceDataInfo(Long id, String InfoId, long testTime,
            String testName, String testPosition, String cableLength,
            String cableType, String faultType, String faultLength,
            String shortNote, String dataCollection, String reportStatus,
            String replyStatus, String replyContent, int magneticFieldGain,
            int VoiceGain, int filterMode, String language) {
        this.id = id;
        this.InfoId = InfoId;
        this.testTime = testTime;
        this.testName = testName;
        this.testPosition = testPosition;
        this.cableLength = cableLength;
        this.cableType = cableType;
        this.faultType = faultType;
        this.faultLength = faultLength;
        this.shortNote = shortNote;
        this.dataCollection = dataCollection;
        this.reportStatus = reportStatus;
        this.replyStatus = replyStatus;
        this.replyContent = replyContent;
        this.magneticFieldGain = magneticFieldGain;
        this.VoiceGain = VoiceGain;
        this.filterMode = filterMode;
        this.language = language;
    }
    @Generated(hash = 1990473192)
    public AssistanceDataInfo() {
    }

   

}

