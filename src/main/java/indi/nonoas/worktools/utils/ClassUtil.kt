package indi.nonoas.worktools.utils

import cn.hutool.core.collection.EnumerationIter
import cn.hutool.core.io.resource.ResourceUtil
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.JarURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.*
import java.util.jar.JarFile

/**
 * @author Nonoas
 * @date 2022/7/16
 */
object ClassUtil {
    /**
     * 通过包名获取包内所有类
     *
     * @param pkg
     * @return
     */
    fun getAllClassByPackage(pkg: Package): List<Class<*>> {
        val packageName = pkg.name
        // 获取当前包下以及子包下所以的类
        return getClassesByPkgName(packageName)
    }

    /**
     * 通过接口名取得某个接口下所有实现这个接口的类
     */
    fun getAllClassByInterface(c: Class<*>): List<Class<*>?>? {
        var returnClassList: MutableList<Class<*>?>? = null
        if (c.isInterface) {
            // 获取当前的包名
            val packageName = c.getPackage().name
            // 获取当前包下以及子包下所以的类
            val allClass = getClassesByPkgName(packageName)
            returnClassList = ArrayList()
            for (cls: Class<*> in allClass) {
                // 判断是否是同一个接口
                if (c.isAssignableFrom(cls)) {
                    // 本身不加入进去
                    if (c != cls) {
                        returnClassList.add(cls)
                    }
                }
            }
        }
        return returnClassList
    }

    /**
     * 取得某一类所在包的所有类名 不含迭代
     */
    fun getPackageAllClassName(classLocation: String?, packageName: String): Array<String>? {
        // 将packageName分解
        val packagePathSplit = packageName.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val realClassLocation = StringBuilder(classLocation)
        val packageLength = packagePathSplit.size
        for (s: String? in packagePathSplit) {
            realClassLocation.append(File.separator).append(s)
        }
        val packeageDir = File(realClassLocation.toString())
        return if (packeageDir.isDirectory()) {
            packeageDir.list()
        } else null
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName 包名
     */
    fun getClassesByPkgName(packageName: String): List<Class<*>> {

        // 第一个class类的集合
        var packageName = packageName
        val classes: MutableList<Class<*>> = ArrayList()
        // 是否循环迭代
        val recursive = true
        // 获取包的名字 并进行替换
        val packageDirName = packageName.replace('.', '/')
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        val dirs: EnumerationIter<URL>
        try {
            dirs = ResourceUtil.getResourceIter(packageDirName)
            // 循环迭代下去
            while (dirs.hasNext()) {
                // 获取下一个元素
                val url = dirs.next()
                // 得到协议的名称
                val protocol = url.protocol
                // 如果是以文件的形式保存在服务器上
                if (("file" == protocol)) {
                    // 获取包的物理路径
                    val filePath = URLDecoder.decode(url.file, "UTF-8")
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes)
                } else if (("jar" == protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    var jar: JarFile
                    try {
                        // 获取jar
                        jar = (url.openConnection() as JarURLConnection).jarFile
                        // 从此jar包 得到一个枚举类
                        val entries = jar.entries()
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            val entry = entries.nextElement()
                            var name = entry.name
                            // 如果是以/开头的
                            if (name[0] == '/') {
                                // 获取后面的字符串
                                name = name.substring(1)
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                val idx = name.lastIndexOf('/')
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.')
                                }
                                // 如果可以迭代下去 并且是一个包
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory) {
                                    // 去掉后面的".class" 获取真正的类名
                                    val className = name.substring(packageName.length + 1, name.length - 6)
                                    try {
                                        // 添加到classes
                                        classes.add(Class.forName("$packageName.$className"))
                                    } catch (e: ClassNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classes
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private fun findAndAddClassesInPackageByFile(
        packageName: String,
        packagePath: String,
        recursive: Boolean,
        classes: MutableList<Class<*>>
    ) {
        // 获取此包的目录 建立一个File
        val dir = File(packagePath)
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return
        }
        // 如果存在 就获取包下的所有文件 包括目录
        val dirfiles = dir.listFiles(FileFilter { file ->
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            (recursive && file.isDirectory()) || (file.getName().endsWith(".class"))
        })
        // 循环所有文件
        for (file: File in dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(
                    packageName + "." + file.getName(),
                    file.absolutePath,
                    recursive,
                    classes
                )
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                val className = file.getName().substring(0, file.getName().length - 6)
                try {
                    // 添加到集合中去
                    classes.add(Class.forName("$packageName.$className"))
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        InvocationTargetException::class,
        NoSuchMethodException::class
    )
    fun newInstance(aClass: Class<*>): Any {
        return aClass.getDeclaredConstructor().newInstance()
    }
}
