1. ## 安装libfatscommon

```shell
yum install  gcc gcc-c++ make automake autoconf libtool pcre pcre-devel zlib zlib-devel openssl-devel -y
git clone https://github.com/happyfish100/libfastcommon.git --depth 1
cd libfastcommon/
./make.sh && ./make.sh install
```



## 2.安装FastDFS

```shell
git clone https://github.com/happyfish100/fastdfs.git --depth 1

cd fastdfs/
./make.sh && ./make.sh install
#配置文件准备
cp /etc/fdfs/tracker.conf.sample /etc/fdfs/tracker.conf
cp /etc/fdfs/storage.conf.sample /etc/fdfs/storage.conf && \
cp /etc/fdfs/client.conf.sample /etc/fdfs/client.conf && \ #客户端文件，测试用
cp /usr/local/src/fastdfs/conf/http.conf /etc/fdfs/  && \ #供nginx访问使用
cp /usr/local/src/fastdfs/conf/mime.types /etc/fdfs/ #供nginx访问使用
```



## 3安装 fastdfs-nginx-module

```shell
git clone https://github.com/happyfish100/fastdfs-nginx-module.git --depth 1
cp mod_fastdfs.conf /etc/fdfs
```

##  4 安装ngnix

```
curl -# -C - -LO  http://nginx.org/download/nginx-1.15.4.tar.gz
tar -xf nginx-1.15.4.tar.gz
./configure --add-module=/opt/fastdfs-nginx-module/src
make && make install #编译安装
```



# 单机部署

## tracker配置

```shell
#服务器ip为 192.168.52.1
#我建议用ftp下载下来这些文件 本地修改
vim /etc/fdfs/tracker.conf
#需要修改的内容如下
port=22122  # tracker服务器端口（默认22122,一般不修改）
base_path=/home/dfs  # 存储日志和数据的根目录
```

## storage配置

```shell
vim /etc/fdfs/storage.conf
#需要修改的内容如下
port=23000  # storage服务端口（默认23000,一般不修改）
base_path=/home/dfs  # 数据和日志文件存储根目录
store_path0=/home/dfs  # 第一个存储目录
tracker_server=192.168.52.1:22122  # tracker服务器IP和端口
http.server_port=8888  # http访问文件的端口(默认8888,看情况修改,和nginx中保持一致)
```

## 配置nginx访问

```
vim /etc/fdfs/mod_fastdfs.conf
#需要修改的内容如下
tracker_server=192.168.52.1:22122  #tracker服务器IP和端口
url_have_group_name=true
store_path0=/home/dfs
#配置nginx.config
vim /usr/local/nginx/conf/nginx.conf
#添加如下配置
server {
    listen       8888;    ## 该端口为storage.conf中的http.server_port相同
    server_name  localhost;
    location ~/group[0-9]/ {
        ngx_fastdfs_module;
    }
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
    root   html;
    }
}
```



# 分布式部署

## tracker配置

```
#服务器ip为 192.168.52.2,192.168.52.3,192.168.52.4
#我建议用ftp下载下来这些文件 本地修改
vim /etc/fdfs/tracker.conf
#需要修改的内容如下
port=22122  # tracker服务器端口（默认22122,一般不修改）
base_path=/home/dfs  # 存储日志和数据的根目录
```

## storage配置

```
vim /etc/fdfs/storage.conf
#需要修改的内容如下
port=23000  # storage服务端口（默认23000,一般不修改）
base_path=/home/dfs  # 数据和日志文件存储根目录
store_path0=/home/dfs  # 第一个存储目录
tracker_server=192.168.52.2:22122  # 服务器1
tracker_server=192.168.52.3:22122  # 服务器2
tracker_server=192.168.52.4:22122  # 服务器3
http.server_port=8888  # http访问文件的端口(默认8888,看情况修改,和nginx中保持一致)
```

## client测试

```
vim /etc/fdfs/client.conf
#需要修改的内容如下
base_path=/home/moe/dfs
tracker_server=192.168.52.2:22122  # 服务器1
tracker_server=192.168.52.3:22122  # 服务器2
tracker_server=192.168.52.4:22122  # 服务器3
#保存后测试,返回ID表示成功 如：group1/M00/00/00/xx.tar.gz
fdfs_upload_file /etc/fdfs/client.conf /usr/local/src/nginx-1.15.4.tar.gz
```

## 配置nginx访问

```shell
vim /etc/fdfs/mod_fastdfs.conf
#需要修改的内容如下
tracker_server=192.168.52.2:22122  # 服务器1
tracker_server=192.168.52.3:22122  # 服务器2
tracker_server=192.168.52.4:22122  # 服务器3
url_have_group_name=true
store_path0=/home/dfs
#配置nginx.config
vim /usr/local/nginx/conf/nginx.conf
#添加如下配置
server {
    listen       8888;    ## 该端口为storage.conf中的http.server_port相同
    server_name  localhost;
    location ~/group[0-9]/ {
        ngx_fastdfs_module;
    }
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
    root   html;
    }
}
```

# 启动

## 防火墙

```shell
#不关闭防火墙的话无法使用
systemctl stop firewalld.service #关闭
systemctl restart firewalld.service #重启
```

## tracker

```shell
/etc/init.d/fdfs_trackerd start #启动tracker服务
/etc/init.d/fdfs_trackerd restart #重启动tracker服务
/etc/init.d/fdfs_trackerd stop #停止tracker服务
chkconfig fdfs_trackerd on #自启动tracker服务
```

## storage

```shell
/etc/init.d/fdfs_storaged start #启动storage服务
/etc/init.d/fdfs_storaged restart #重动storage服务
/etc/init.d/fdfs_storaged stop #停止动storage服务
chkconfig fdfs_storaged on #自启动storage服务
```

## nginx

```shell
/usr/local/nginx/sbin/nginx #启动nginx
/usr/local/nginx/sbin/nginx -s reload #重启nginx
/usr/local/nginx/sbin/nginx -s stop #停止nginx
```

## 检测集群

```shell
/usr/bin/fdfs_monitor /etc/fdfs/storage.conf
# 会显示会有几台服务器 有3台就会 显示 Storage 1-Storage 3的详细信息
```

# 说明

## 配置文件

```shell
tracker_server #有几台服务器写几个
group_name #地址的名称的命名
bind_addr #服务器ip绑定
store_path_count #store_path(数字)有几个写几个
store_path(数字) #设置几个储存地址写几个 从0开始
```



springboot fast-FDFS

```
仓库地址
#http://maven.yonyoucloud.com/nexus/content/repositories/releases/

```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.7.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.app</groupId>
	<artifactId>springboot-fastFDFS</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>springboot-fastFDFS</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.csource/fastdfs-client-java -->
		<dependency>
			<groupId>org.csource</groupId>
			<artifactId>fastdfs-client-java</artifactId>
			<version>1.27-RELEASE</version>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
	<repositories>
		<repository>
			<id>fastdfs</id>
			<url>http://maven.yonyoucloud.com/nexus/content/repositories/releases/</url>
		</repository>
	</repositories>
</project>

```

## 配置文件

