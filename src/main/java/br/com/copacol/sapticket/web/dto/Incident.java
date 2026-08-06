package br.com.copacol.sapticket.web.dto;

import org.springframework.web.service.invoker.HttpRequestValues.Metadata;

public class Incident {
     private Metadata __metadata;
    private String Guid;
    private String TextMode;
    private String ProcessType;
    private String ObjectId;
    private String Description;
    private String CategoryAspectId;
    private String CategoryId;
    private String CategoryCatalogType;
    private String SAPComponent;
    private String LongText;
    private String Priority;
    private String ConfigurationItemId;
    private String Partner1;
    private String PartnerFct1;
    private String Partner2;
    private String PartnerFct2;
    private String Partner3;
    private String PartnerFct3;
    private AttachmentSet AttachmentSet;
    public Metadata get__metadata() {
        return __metadata;
    }
    public void set__metadata(Metadata __metadata) {
        this.__metadata = __metadata;
    }
    public String getGuid() {
        return Guid;
    }
    public void setGuid(String guid) {
        Guid = guid;
    }
    public String getTextMode() {
        return TextMode;
    }
    public void setTextMode(String textMode) {
        TextMode = textMode;
    }
    public String getProcessType() {
        return ProcessType;
    }
    public void setProcessType(String processType) {
        ProcessType = processType;
    }
    public String getObjectId() {
        return ObjectId;
    }
    public void setObjectId(String objectId) {
        ObjectId = objectId;
    }
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }
    public String getCategoryAspectId() {
        return CategoryAspectId;
    }
    public void setCategoryAspectId(String categoryAspectId) {
        CategoryAspectId = categoryAspectId;
    }
    public String getCategoryId() {
        return CategoryId;
    }
    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }
    public String getCategoryCatalogType() {
        return CategoryCatalogType;
    }
    public void setCategoryCatalogType(String categoryCatalogType) {
        CategoryCatalogType = categoryCatalogType;
    }
    public String getSAPComponent() {
        return SAPComponent;
    }
    public void setSAPComponent(String sAPComponent) {
        SAPComponent = sAPComponent;
    }
    public String getLongText() {
        return LongText;
    }
    public void setLongText(String longText) {
        LongText = longText;
    }
    public String getPriority() {
        return Priority;
    }
    public void setPriority(String priority) {
        Priority = priority;
    }
    public String getConfigurationItemId() {
        return ConfigurationItemId;
    }
    public void setConfigurationItemId(String configurationItemId) {
        ConfigurationItemId = configurationItemId;
    }
    public String getPartner1() {
        return Partner1;
    }
    public void setPartner1(String partner1) {
        Partner1 = partner1;
    }
    public String getPartnerFct1() {
        return PartnerFct1;
    }
    public void setPartnerFct1(String partnerFct1) {
        PartnerFct1 = partnerFct1;
    }
    public String getPartner2() {
        return Partner2;
    }
    public void setPartner2(String partner2) {
        Partner2 = partner2;
    }
    public String getPartnerFct2() {
        return PartnerFct2;
    }
    public void setPartnerFct2(String partnerFct2) {
        PartnerFct2 = partnerFct2;
    }
    public String getPartner3() {
        return Partner3;
    }
    public void setPartner3(String partner3) {
        Partner3 = partner3;
    }
    public String getPartnerFct3() {
        return PartnerFct3;
    }
    public void setPartnerFct3(String partnerFct3) {
        PartnerFct3 = partnerFct3;
    }
    public AttachmentSet getAttachmentSet() {
        return AttachmentSet;
    }
    public void setAttachmentSet(AttachmentSet attachmentSet) {
        AttachmentSet = attachmentSet;
    }
}
