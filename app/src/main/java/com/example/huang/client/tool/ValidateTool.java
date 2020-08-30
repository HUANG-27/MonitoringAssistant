package com.example.huang.client.tool;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class ValidateTool {

    // 验证手机号码
    public static boolean phoneNumberValidate(String phoneNumber) {
        /*
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @com.example.huang.suspicious_target_monitoring_assistant.entity.param str
         * @return 待检测的字符串
         */
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";
        if (TextUtils.isEmpty(phoneNumber))
            return true;
        else
            return !phoneNumber.matches(telRegex);
    }

    // 验证密码
    public static boolean passwordValidate(String password) {
        /**
         * ASCII
         * 数字 48-57
         * 大写字母 65-90
         * 小写字母 97-122
         */
        if(password.length() == 0)
            return true;

        for (int i = 0; i < password.length(); i++) {
            int chr = password.charAt(i);
            if (chr < 48)
                return true;
            if (chr > 57 && chr < 65)
                return true;
            if (chr > 90 && chr < 97)
                return true;
            if (chr > 122)
                return true;
        }
        return false;
    }

    // 身份证号校验
    public static class IDNumberValidate {
        private static final String[] ValCodeArr = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        private static final String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};
        // 身份证的最小出生日期,1900年1月1日
        private final static Date MINIMAL_BIRTH_DATE = new Date(-2209017600000L);
        private static final String BIRTH_DATE_FORMAT = "yyyyMMdd";
        private final static int NEW_CARD_NUMBER_LENGTH = 18;
        private final static int OLD_CARD_NUMBER_LENGTH = 15;
        private final static String LENGTH_ERROR = "身份证长度必须为15或者18位！";
        private final static String NUMBER_ERROR = "15位身份证都应该为数字，18位身份证都应该前17位应该都为数字！";
        private final static String DATE_ERROR = "身份证日期验证无效！";
        private final static String AREA_ERROR = "身份证地区编码错误!";
        private final static String CHECK_CODE_ERROR = "身份证最后一位校验码有误！";

        /**
         * 身份证校验
         *
         * @param idNumber 需要验证的身份证
         * @return 身份证无误返回传入的身份证号
         */
        public static boolean validate(String idNumber) {
            String Ai = idNumber.trim();
            System.out.println(Ai.length() != 15);
            if (Ai.length() == 15 | Ai.length() == 18) {
                //如果为15位则自动补全到18位
                if (Ai.length() == OLD_CARD_NUMBER_LENGTH) {
                    Ai = convertToNewIdNumber(Ai);
                }
            } else {
                //LENGTH_ERROR
                return false;
            }
            // 身份证号的前15,17位必须是阿拉伯数字
            for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
                char ch = Ai.charAt(i);
                if (ch < '0' || ch > '9') {
                    return false;
                }
            }
            //校验身份证日期信息是否有效 ，出生日期不能晚于当前时间，并且不能早于1900年
            try {
                Date birthDate = getBirthDate(Ai);
                if (!birthDate.before(new Date())) {
                    //DATE_ERROR
                    return false;
                }
                if (!birthDate.after(MINIMAL_BIRTH_DATE)) {
                    //DATE_ERROR
                    return false;
                }
                String birthdayPart = getBirthDayPart(Ai);
                String realBirthdayPart = createBirthDateParser().format(birthDate);
                if (!birthdayPart.equals(realBirthdayPart)) {
                    //DATE_ERROR
                    return false;
                }
            } catch (Exception e) {
                //DATE_ERROR
                return false;
            }
            //校验地区码是否正确
            Hashtable<String, String> h = getAreaCode();
            if (h.get(Ai.substring(0, 2)) == null) {
                //AREA_ERROR
                return false;
            }
            //校验身份证最后一位 身份证校验码
            //CHECK_CODE_ERROR
            //通过验证则返回true
            return calculateVerifyCode(Ai).equals(String.valueOf(Ai.charAt(NEW_CARD_NUMBER_LENGTH - 1)));

        }

        /**
         * 把15位身份证号码转换到18位身份证号码<br>
         * 15位身份证号码与18位身份证号码的区别为：<br>
         * 1、15位身份证号码中，"出生年份"字段是2位，转换时需要补入"19"，表示20世纪<br>
         * 2、15位身份证无最后一位校验码。18位身份证中，校验码根据根据前17位生成
         *
         * @return 转换后的身份证号
         */
        private static String convertToNewIdNumber(String oldIdNumber) {
            StringBuilder buf = new StringBuilder(NEW_CARD_NUMBER_LENGTH);
            buf.append(oldIdNumber.substring(0, 6));
            buf.append("19");
            buf.append(oldIdNumber.substring(6));
            buf.append(calculateVerifyCode(buf));
            return buf.toString();
        }

        /**
         * 计算最后一位校验码 加权值%11
         * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
         * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
         * （2）计算模 Y = mod(S, 11)
         * （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
         *
         * @param idNumber 身份证号
         * @return 校验码
         */
        private static String calculateVerifyCode(CharSequence idNumber) {
            int sum = 0;
            for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
                char ch = idNumber.charAt(i);
                sum += ((ch - '0')) * Integer.parseInt(Wi[i]);
            }
            return ValCodeArr[sum % 11];
        }

        /**
         * 地区编码哈希表
         *
         * @return Hashtable 对象
         */
        private static Hashtable<String, String> getAreaCode() {
            Hashtable<String, String> hashtable = new Hashtable<String, String>();
            hashtable.put("11", "北京");
            hashtable.put("12", "天津");
            hashtable.put("13", "河北");
            hashtable.put("14", "山西");
            hashtable.put("15", "内蒙古");
            hashtable.put("21", "辽宁");
            hashtable.put("22", "吉林");
            hashtable.put("23", "黑龙江");
            hashtable.put("31", "上海");
            hashtable.put("32", "江苏");
            hashtable.put("33", "浙江");
            hashtable.put("34", "安徽");
            hashtable.put("35", "福建");
            hashtable.put("36", "江西");
            hashtable.put("37", "山东");
            hashtable.put("41", "河南");
            hashtable.put("42", "湖北");
            hashtable.put("43", "湖南");
            hashtable.put("44", "广东");
            hashtable.put("45", "广西");
            hashtable.put("46", "海南");
            hashtable.put("50", "重庆");
            hashtable.put("51", "四川");
            hashtable.put("52", "贵州");
            hashtable.put("53", "云南");
            hashtable.put("54", "西藏");
            hashtable.put("61", "陕西");
            hashtable.put("62", "甘肃");
            hashtable.put("63", "青海");
            hashtable.put("64", "宁夏");
            hashtable.put("65", "新疆");
            hashtable.put("71", "台湾");
            hashtable.put("81", "香港");
            hashtable.put("82", "澳门");
            hashtable.put("91", "国外");
            return hashtable;
        }

        //从身份证号读取生日并校验
        private static Date getBirthDate(String cardNum) {
            Date cacheBirthDate = null;
            try {
                cacheBirthDate = createBirthDateParser().parse(getBirthDayPart(cardNum));
            } catch (Exception e) {
                throw new RuntimeException("身份证的出生日期无效");
            }
            assert cacheBirthDate != null;
            return new Date(cacheBirthDate.getTime());
        }
        @SuppressLint("SimpleDateFormat")
        private static SimpleDateFormat createBirthDateParser() {
            return new SimpleDateFormat(BIRTH_DATE_FORMAT);
        }
        private static String getBirthDayPart(String cardNum) {
            return cardNum.substring(6, 14);
        }
    }

}
