package indi.nonoas.worktools.platform.common;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author huangshengsheng
 * @date 2025/12/2 17:56
 */
public enum ColorSerials {

    GREEN("#81D19D", "#5FAAD1", "#FFEEB2", "#FF806E"),
    ;

    private final String[] colorArray;

    ColorSerials(String... colors) {
        colorArray = colors;
    }

    /**
     * 使用 SecureRandom 从颜色组中随机获取一个颜色。
     *
     * @return 随机选中的颜色（String类型，十六进制代码）。
     */
    public String getSecureRandomColor() {
        // 1. 获取 SecureRandom 实例
        SecureRandom secureRandom;
        try {
            // 尝试获取一个推荐的安全随机数生成器
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            secureRandom = new SecureRandom();
        }

        // 2. 获取颜色数组的长度
        int arrayLength = colorArray.length;
        int randomIndex = secureRandom.nextInt(arrayLength);

        // 4. 根据随机索引返回对应的颜色
        return colorArray[randomIndex];
    }
}