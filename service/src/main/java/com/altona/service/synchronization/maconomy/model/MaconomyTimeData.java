package com.altona.service.synchronization.maconomy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MaconomyTimeData {

    // Project Data
    @Setter
    private String customernumbervar;
    @Setter
    private String customernamevar;
    @Setter
    private String jobnumber;

    // Job Data

    @Setter
    private String taskname;
    @Setter
    private String tasktextvar;
    @Setter
    private String jobnamevar;

    // Time Data
    @Setter
    private BigDecimal numberday1;
    @Setter
    private BigDecimal numberday2;
    @Setter
    private BigDecimal numberday3;
    @Setter
    private BigDecimal numberday4;
    @Setter
    private BigDecimal numberday5;
    @Setter
    private BigDecimal numberday6;
    @Setter
    private BigDecimal numberday7;

    private String linedetailstypevar;
    private String employeenumber;
    private Integer linenumber;
    private String periodstart;
    private String activitynumber;
    private String entrytext;
    private String activitytype;
    private Integer numberday1transferred;
    private Integer numberday2transferred;
    private Integer numberday3transferred;
    private Integer numberday4transferred;
    private Integer numberday5transferred;
    private Integer numberday6transferred;
    private Integer numberday7transferred;
    private String remark;
    private String description;
    private Boolean internaljob;
    private String specification1name;
    private String specification2name;
    private String specification3name;
    private Integer timeactivity1number;
    private Integer timeactivity2number;
    private Integer timeactivity3number;
    private Integer timeactivity1transferred;
    private Integer timeactivity2transferred;
    private Integer timeactivity3transferred;
    private Integer amountactivity1number;
    private Integer amountactivity2number;
    private Integer amountactivity3number;
    private Integer amountactivity4number;
    private Integer amountactivity1transferred;
    private Integer amountactivity2transferred;
    private Integer amountactivity3transferred;
    private Integer amountactivity4transferred;
    private String locationname;
    private String entityname;
    private String projectname;
    private String purposename;
    private String localspec1name;
    private String localspec2name;
    private String localspec3name;
    private String companynumber;
    private Integer quantitya;
    private Integer quantityb;
    private Integer quantityatransferred;
    private Integer quantitybtransferred;
    private Boolean transferredforposting;
    private Double weektotal;
    private String costtype;
    private Boolean submitted;
    private String newapprovalstatus;
    private String approvalstatus;
    private String approvedorrejectedby;
    private String approvaldate;
    private String commentprojectmanager;
    private Boolean approvedbysuperior;
    private Boolean releasedbysuperior;
    private Integer daylinenumberday1;
    private Integer daylinenumberday2;
    private Integer daylinenumberday3;
    private Integer daylinenumberday4;
    private Integer daylinenumberday5;
    private Integer daylinenumberday6;
    private Integer daylinenumberday7;
    private Boolean permanentline;
    private Integer plannedfortheperiod;
    private Integer estimatedtimetocompletion;
    private Boolean executed;
    private String estimatedate;
    private Integer jobbudgetlinenumber;
    private String jobbudgettype;
    private Integer jobbudgetrevisionnumber;
    private String jobbudgetlineinstancekey;
    private String instancekey;
    private String overtimetype;
    private String favorite;
    private String registrationnote;
    private Integer estimatedtimetocompletionmandays;
    private String descriptionday1;
    private String descriptionday2;
    private String descriptionday3;
    private String descriptionday4;
    private String descriptionday5;
    private String descriptionday6;
    private String descriptionday7;
    private String transactiontimestamp;
    private Double numberproposed;
    private Integer numberproposedday1;
    private Integer numberproposedday2;
    private Double numberproposedday3;
    private Double numberproposedday4;
    private Integer numberproposedday5;
    private Integer numberproposedday6;
    private Integer numberproposedday7;
    private Integer numberproposedday1transferred;
    private Integer numberproposedday2transferred;
    private Integer numberproposedday3transferred;
    private Integer numberproposedday4transferred;
    private Integer numberproposedday5transferred;
    private Integer numberproposedday6transferred;
    private Integer numberproposedday7transferred;
    private Integer billingpricetotalproposedcurrency;
    private Integer billingpriceproposedcurrency;
    private Integer billingpricecurrency;
    private Boolean useinvoiceproposal;
    private String transactiontype;
    private Boolean invoiceable;
    private Integer billingpricebase;
    private Integer billingpriceenterprise;
    private String employeecategorynumber;
    private String absencetype;
    private String timeregistrationunit;
    private String optionlistnumber1;
    private String selectedoption1;
    private String optionlistnumber2;
    private String selectedoption2;
    private String optionlistnumber3;
    private String selectedoption3;
    private String optionlistnumber4;
    private String selectedoption4;
    private String optionlistnumber5;
    private String selectedoption5;
    private String specification4name;
    private String specification5name;
    private String specification6name;
    private String specification7name;
    private String specification8name;
    private String specification9name;
    private String specification10name;
    private String localspec4name;
    private String localspec5name;
    private String localspec6name;
    private String localspec7name;
    private String localspec8name;
    private String localspec9name;
    private String localspec10name;
    private Integer purchaseordernumber;
    private Integer purchaseorderlinenumber;
    private String activitytextvar;
    private String taskpathvar;
    private String linecompanynamevar;
    private String locationdescriptionvar;
    private String entitydescriptionvar;
    private String projectdescriptionvar;
    private String purposedescriptionvar;
    private String specification1descriptionvar;
    private String specification2descriptionvar;
    private String specification3descriptionvar;
    private String specification4descriptionvar;
    private String specification5descriptionvar;
    private String specification6descriptionvar;
    private String specification7descriptionvar;
    private String specification8descriptionvar;
    private String specification9descriptionvar;
    private String specification10descriptionvar;
    private String localspec1descriptionvar;
    private String localspec2descriptionvar;
    private String localspec3descriptionvar;
    private String localspec4descriptionvar;
    private String localspec5descriptionvar;
    private String localspec6descriptionvar;
    private String localspec7descriptionvar;
    private String localspec8descriptionvar;
    private String localspec9descriptionvar;
    private String localspec10descriptionvar;
    private Integer linetotalformonthvar;
    private String linecustomerpopup5var;
    private String linecustomerremark15var;
    private String linecustomerremark16var;
    private String linecustomerremark17var;
    private String linecustomerremark18var;
    private String linecustomerremark19var;
    private String linecustomerremark20var;
    private String weektextheaderinstancekeyvar;
    private String day1textheaderinstancekeyvar;
    private String day2textheaderinstancekeyvar;
    private String day3textheaderinstancekeyvar;
    private String day4textheaderinstancekeyvar;
    private String day5textheaderinstancekeyvar;
    private String day6textheaderinstancekeyvar;
    private String day7textheaderinstancekeyvar;
    private Boolean weektextheaderhastextvar;
    private Boolean day1textheaderhastextvar;
    private Boolean day2textheaderhastextvar;
    private Boolean day3textheaderhastextvar;
    private Boolean day4textheaderhastextvar;
    private Boolean day5textheaderhastextvar;
    private Boolean day6textheaderhastextvar;
    private Boolean day7textheaderhastextvar;
    private Boolean weektextheaderallowchangevar;
    private Boolean day1textheaderallowchangevar;
    private Boolean day2textheaderallowchangevar;
    private Boolean day3textheaderallowchangevar;
    private Boolean day4textheaderallowchangevar;
    private Boolean day5textheaderallowchangevar;
    private Boolean day6textheaderallowchangevar;
    private Boolean day7textheaderallowchangevar;
    private String languagevar;
    private Integer expenseamountbasetotalvar;
    private Integer mileageamountbasetotalvar;
    private Integer mileagenumberoftotalvar;
    private Integer billingpricetotalcurrencyvar;
    private Integer billingpricetotalbasevar;
    private Integer billingpricetotalenterprisevar;
    private String tooltipjobvar;
    private String tooltiptaskvar;
    private String tooltiptasklinebreakvar;
    private String tooltipactivityvar;
    private String tooltipconclusionvar;
    private Boolean usesdailydescriptionsvar;
    private String customerlevelnamevar;
    private String level1customernumbervar;
    private String level2customernumbervar;
    private String level3customernumbervar;
    private String level4customernumbervar;
    private String level5customernumbervar;
    private String level1customernamevar;
    private String level2customernamevar;
    private String level3customernamevar;
    private String level4customernamevar;
    private String level5customernamevar;
    private String lineapprovalrelationvar;
    private Integer lineapprovalnumbervar;
    private Integer lineapprovallinenumbervar;
    private String linecurrentapprovalstatusdescriptionvar;
    private String linecurrentapprovalstatusvar;
    private Boolean linecanbeapprovedbycurrentuservar;
    private String lineapprovedorrejectedbyvar;
    private String lineapprovaldatevar;
    private String lineapprovaltimevar;
    private String lineremarkvar;
    private Integer timeroundingunitvar;

}