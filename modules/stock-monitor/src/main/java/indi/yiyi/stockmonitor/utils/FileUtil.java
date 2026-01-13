package indi.yiyi.stockmonitor.utils; // 建议放在一个工具类包下

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileUtil {

    /**
     * 从项目启动时的当前工作目录下的指定路径读取文本内容。
     *
     * 路径示例：项目根目录/config/context
     *
     * @param relativePath 相对于项目同级目录的路径，例如："config/context"
     * @return 文件的全部文本内容
     * @throws IOException 如果文件不存在、无法读取或发生其他I/O错误
     */
    public static String readTextFromProjectRelativePath(String relativePath) throws IOException {

        // 1. 获取当前工作目录 (Current Working Directory, CWD)
        // CWD 是 Java 应用程序运行时所在的目录，通常是项目根目录。
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        // 2. 构造完整的绝对路径
        // Paths.get(basePath, relativePath) 会将两者安全地组合起来。
        Path absolutePath = currentDir.resolve(relativePath);

        // 打印路径，方便调试
        System.out.println("尝试读取文件路径: " + absolutePath.toAbsolutePath());

        if (!Files.exists(absolutePath)) {
            // 如果文件不存在，抛出更具体的异常
            throw new IOException("文件不存在: " + absolutePath.toAbsolutePath());
        }

        // 3. 读取文件的所有行并合并成一个字符串
        // Files.readAllLines() 适用于中小型的文本文件。
        // 使用 Collectors.joining("\n") 确保行尾换行符被保留。
        String content = Files.readAllLines(absolutePath)
                .stream()
                .collect(Collectors.joining("\n"));

        return content;
    }
}