package dev.practice.webclient.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExchangeRatesResponse {

    private final Double USD;
    private final Double AED;
    private final Double AFN;
    private final Double ALL;
    private final Double AMD;
    private final Double ANG;
    private final Double AOA;
    private final Double ARS;
    private final Double AUD;
    private final Double AWG;
    private final Double AZN;
    private final Double BAM;
    private final Double BBD;
    private final Double BDT;
    private final Double BGN;
    private final Double BHD;
    private final Double BIF;
    private final Double BMD;
    private final Double BND;
    private final Double BOB;
    private final Double BRL;
    private final Double BSD;
    private final Double BTN;
    private final Double BWP;
    private final Double BYN;
    private final Double BZD;
    private final Double CAD;
    private final Double CDF;
    private final Double CHF;
    private final Double CLP;
    private final Double CNY;
    private final Double COP;
    private final Double CRC;
    private final Double CUP;
    private final Double CVE;
    private final Double CZK;
    private final Double DJF;
    private final Double DKK;
    private final Double DOP;
    private final Double DZD;
    private final Double EGP;
    private final Double ERN;
    private final Double ETB;
    private final Double EUR;
    private final Double FJD;
    private final Double FKP;
    private final Double FOK;
    private final Double GBP;
    private final Double GEL;
    private final Double GGP;
    private final Double GHS;
    private final Double GIP;
    private final Double GMD;
    private final Double GNF;
    private final Double GTQ;
    private final Double GYD;
    private final Double HKD;
    private final Double HNL;
    private final Double HRK;
    private final Double HTG;
    private final Double HUF;
    private final Double IDR;
    private final Double ILS;
    private final Double IMP;
    private final Double INR;
    private final Double IQD;
    private final Double IRR;
    private final Double ISK;
    private final Double JEP;
    private final Double JMD;
    private final Double JOD;
    private final Double JPY;
    private final Double KES;
    private final Double KGS;
    private final Double KHR;
    private final Double KID;
    private final Double KMF;
    private final Double KRW;
    private final Double KWD;
    private final Double KYD;
    private final Double KZT;
    private final Double LAK;
    private final Double LBP;
    private final Double LKR;
    private final Double LRD;
    private final Double LSL;
    private final Double LYD;
    private final Double MAD;
    private final Double MDL;
    private final Double MGA;
    private final Double MKD;
    private final Double MMK;
    private final Double MNT;
    private final Double MOP;
    private final Double MRU;
    private final Double MUR;
    private final Double MVR;
    private final Double MWK;
    private final Double MXN;
    private final Double MYR;
    private final Double MZN;
    private final Double NAD;
    private final Double NGN;
    private final Double NIO;
    private final Double NOK;
    private final Double NPR;
    private final Double NZD;
    private final Double OMR;
    private final Double PAB;
    private final Double PEN;
    private final Double PGK;
    private final Double PHP;
    private final Double PKR;
    private final Double PLN;
    private final Double PYG;
    private final Double QAR;
    private final Double RON;
    private final Double RSD;
    private final Double RUB;
    private final Double RWF;
    private final Double SAR;
    private final Double SBD;
    private final Double SCR;
    private final Double SDG;
    private final Double SEK;
    private final Double SGD;
    private final Double SHP;
    private final Double SLE;
    private final Double SLL;
    private final Double SOS;
    private final Double SRD;
    private final Double SSP;
    private final Double STN;
    private final Double SYP;
    private final Double SZL;
    private final Double THB;
    private final Double TJS;
    private final Double TMT;
    private final Double TND;
    private final Double TOP;
    private final Double TRY;
    private final Double TTD;
    private final Double TVD;
    private final Double TWD;
    private final Double TZS;
    private final Double UAH;
    private final Double UGX;
    private final Double UYU;
    private final Double UZS;
    private final Double VES;
    private final Double VND;
    private final Double VUV;
    private final Double WST;
    private final Double XAF;
    private final Double XCD;
    private final Double XDR;
    private final Double XOF;
    private final Double XPF;
    private final Double YER;
    private final Double ZAR;
    private final Double ZMW;
    private final Double ZWL;

    @Builder
    private ExchangeRatesResponse(Double USD, Double AED, Double AFN, Double ALL, Double AMD, Double ANG, Double AOA, Double ARS, Double AUD, Double AWG, Double AZN, Double BAM, Double BBD, Double BDT, Double BGN, Double BHD, Double BIF, Double BMD, Double BND, Double BOB, Double BRL, Double BSD, Double BTN, Double BWP, Double BYN, Double BZD, Double CAD, Double CDF, Double CHF, Double CLP, Double CNY, Double COP, Double CRC, Double CUP, Double CVE, Double CZK, Double DJF, Double DKK, Double DOP, Double DZD, Double EGP, Double ERN, Double ETB, Double EUR, Double FJD, Double FKP, Double FOK, Double GBP, Double GEL, Double GGP, Double GHS, Double GIP, Double GMD, Double GNF, Double GTQ, Double GYD, Double HKD, Double HNL, Double HRK, Double HTG, Double HUF, Double IDR, Double ILS, Double IMP, Double INR, Double IQD, Double IRR, Double ISK, Double JEP, Double JMD, Double JOD, Double JPY, Double KES, Double KGS, Double KHR, Double KID, Double KMF, Double KRW, Double KWD, Double KYD, Double KZT, Double LAK, Double LBP, Double LKR, Double LRD, Double LSL, Double LYD, Double MAD, Double MDL, Double MGA, Double MKD, Double MMK, Double MNT, Double MOP, Double MRU, Double MUR, Double MVR, Double MWK, Double MXN, Double MYR, Double MZN, Double NAD, Double NGN, Double NIO, Double NOK, Double NPR, Double NZD, Double OMR, Double PAB, Double PEN, Double PGK, Double PHP, Double PKR, Double PLN, Double PYG, Double QAR, Double RON, Double RSD, Double RUB, Double RWF, Double SAR, Double SBD, Double SCR, Double SDG, Double SEK, Double SGD, Double SHP, Double SLE, Double SLL, Double SOS, Double SRD, Double SSP, Double STN, Double SYP, Double SZL, Double THB, Double TJS, Double TMT, Double TND, Double TOP, Double TRY, Double TTD, Double TVD, Double TWD, Double TZS, Double UAH, Double UGX, Double UYU, Double UZS, Double VES, Double VND, Double VUV, Double WST, Double XAF, Double XCD, Double XDR, Double XOF, Double XPF, Double YER, Double ZAR, Double ZMW, Double ZWL) {
        this.USD = USD;
        this.AED = AED;
        this.AFN = AFN;
        this.ALL = ALL;
        this.AMD = AMD;
        this.ANG = ANG;
        this.AOA = AOA;
        this.ARS = ARS;
        this.AUD = AUD;
        this.AWG = AWG;
        this.AZN = AZN;
        this.BAM = BAM;
        this.BBD = BBD;
        this.BDT = BDT;
        this.BGN = BGN;
        this.BHD = BHD;
        this.BIF = BIF;
        this.BMD = BMD;
        this.BND = BND;
        this.BOB = BOB;
        this.BRL = BRL;
        this.BSD = BSD;
        this.BTN = BTN;
        this.BWP = BWP;
        this.BYN = BYN;
        this.BZD = BZD;
        this.CAD = CAD;
        this.CDF = CDF;
        this.CHF = CHF;
        this.CLP = CLP;
        this.CNY = CNY;
        this.COP = COP;
        this.CRC = CRC;
        this.CUP = CUP;
        this.CVE = CVE;
        this.CZK = CZK;
        this.DJF = DJF;
        this.DKK = DKK;
        this.DOP = DOP;
        this.DZD = DZD;
        this.EGP = EGP;
        this.ERN = ERN;
        this.ETB = ETB;
        this.EUR = EUR;
        this.FJD = FJD;
        this.FKP = FKP;
        this.FOK = FOK;
        this.GBP = GBP;
        this.GEL = GEL;
        this.GGP = GGP;
        this.GHS = GHS;
        this.GIP = GIP;
        this.GMD = GMD;
        this.GNF = GNF;
        this.GTQ = GTQ;
        this.GYD = GYD;
        this.HKD = HKD;
        this.HNL = HNL;
        this.HRK = HRK;
        this.HTG = HTG;
        this.HUF = HUF;
        this.IDR = IDR;
        this.ILS = ILS;
        this.IMP = IMP;
        this.INR = INR;
        this.IQD = IQD;
        this.IRR = IRR;
        this.ISK = ISK;
        this.JEP = JEP;
        this.JMD = JMD;
        this.JOD = JOD;
        this.JPY = JPY;
        this.KES = KES;
        this.KGS = KGS;
        this.KHR = KHR;
        this.KID = KID;
        this.KMF = KMF;
        this.KRW = KRW;
        this.KWD = KWD;
        this.KYD = KYD;
        this.KZT = KZT;
        this.LAK = LAK;
        this.LBP = LBP;
        this.LKR = LKR;
        this.LRD = LRD;
        this.LSL = LSL;
        this.LYD = LYD;
        this.MAD = MAD;
        this.MDL = MDL;
        this.MGA = MGA;
        this.MKD = MKD;
        this.MMK = MMK;
        this.MNT = MNT;
        this.MOP = MOP;
        this.MRU = MRU;
        this.MUR = MUR;
        this.MVR = MVR;
        this.MWK = MWK;
        this.MXN = MXN;
        this.MYR = MYR;
        this.MZN = MZN;
        this.NAD = NAD;
        this.NGN = NGN;
        this.NIO = NIO;
        this.NOK = NOK;
        this.NPR = NPR;
        this.NZD = NZD;
        this.OMR = OMR;
        this.PAB = PAB;
        this.PEN = PEN;
        this.PGK = PGK;
        this.PHP = PHP;
        this.PKR = PKR;
        this.PLN = PLN;
        this.PYG = PYG;
        this.QAR = QAR;
        this.RON = RON;
        this.RSD = RSD;
        this.RUB = RUB;
        this.RWF = RWF;
        this.SAR = SAR;
        this.SBD = SBD;
        this.SCR = SCR;
        this.SDG = SDG;
        this.SEK = SEK;
        this.SGD = SGD;
        this.SHP = SHP;
        this.SLE = SLE;
        this.SLL = SLL;
        this.SOS = SOS;
        this.SRD = SRD;
        this.SSP = SSP;
        this.STN = STN;
        this.SYP = SYP;
        this.SZL = SZL;
        this.THB = THB;
        this.TJS = TJS;
        this.TMT = TMT;
        this.TND = TND;
        this.TOP = TOP;
        this.TRY = TRY;
        this.TTD = TTD;
        this.TVD = TVD;
        this.TWD = TWD;
        this.TZS = TZS;
        this.UAH = UAH;
        this.UGX = UGX;
        this.UYU = UYU;
        this.UZS = UZS;
        this.VES = VES;
        this.VND = VND;
        this.VUV = VUV;
        this.WST = WST;
        this.XAF = XAF;
        this.XCD = XCD;
        this.XDR = XDR;
        this.XOF = XOF;
        this.XPF = XPF;
        this.YER = YER;
        this.ZAR = ZAR;
        this.ZMW = ZMW;
        this.ZWL = ZWL;
    }
}