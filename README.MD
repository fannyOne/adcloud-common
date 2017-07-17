欢迎使用 ADCloud 敏捷交付云平台
===========

##一、ADCloud简介

ADCloud是一个持续交付云平台，能实现从创建项目，代码管理，关联需求，持续集成，应用部署，生产发布等全流程自动化，信息可视化 。
### 我们的特色
* 以敏捷开发为第一服务对象
* 一键发布的能力
* 可靠的应用服务
### 关于 ADCloud Common
* SpringBoot 、SpringMVC as framework

* Ebean as ORM


二、 开发环境
-----------
###1.开发工具
你可以根据自己的习惯选择你喜欢的开发工具，在这里我们推荐使用IntelliJ IDEA。需要安装以下插件，以IntelliJ IDEA为例。
关于IntelliJ IDEA的使用教程 可以参照以下链接 http://wiki.jikexueyuan.com/project/intellij-idea-tutorial/theme-settings.html



    安装  Lombok Plugin <https://plugins.jetbrains.com/plugin/6317>

###2. JDK 配置
提前安装jdk，配置环境变量

  
    File -->Project Structure -->SDKs  --> JDK --> 选择JDK路径
    

###3. Maven 配置

    File --> Setting -->Maven --> Maven home directory --> 选择Maven路径

三、代码结构
-----------



![这里写图片描述](http://img.blog.csdn.net/20170627140924649?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvenhzX3R5aA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20170627185236921?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvenhzX3R5aA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)




四、项目特殊配置
-----------
###1. 配置文件application.properties


  
    server.context-path=${context-path:/}  //访问方式
    server.session.timeout=10800           //超时时间
    
    //Gitlab配置
    gitlab.server.url=http://www.baidu.com //gitlab服务器地址
    gitlab.admin.token=*******             // 管理员token
    gitlab.username=hello                  // gitlab管理用户名
    gitlab.password=password               // gitlab管理密码
    pagesize=10

    //Artifactory配置
    artifactory.user.url=http://www.baidu.com //artifactory仓库地址
    artifactory.user.name=name                //登录名
    artifactory.user.password=password        //密码
    
    
    //Redis配置
    spring.redis.hosts=ip:port

    //Dcos配置
    dcosApi.url=http://www.baidu.com         //dcosApi地址
    
    //邮件发送相关参数配置
    mail.smtp.url=http://www.baidu.com       //邮件服务器地址
    mail.username=hello                      //邮箱用户名
    mail.password=12345678                   //邮箱密码
    
 
   
###2.环境区分配置
项目中可以根据不同的环境配置相应的配置文件，以不同的文件名来区分不同的环境。
格式如下：application- {profile} .properties
例如：用qa来表示测试环境，prod来表示生产环境，配置如下：

    application-qa.properties
    application-prod.properties
    
    在启动程序的时候通过添加 –spring.profiles.active={profile} 来指定具体使用的配置 
    以Application-qa.properties为例具体说明如下：
    
    logging.path=/app/logs              //日志文件路径
    spring.redis.hosts=ip:port          //redis地址
    dcosApi.url=http://www.sohu.com     //dcosApi地址
    datasource.url=databaseUrl          //数据库url
    datasource.username=adcloud         //数据库登录名
    datasource.password=adcloud         //数据库访问密码
    sonar.datasource.url=xxx            //sonar数据库url
    sonar.datasource.username=xxx       //sonar数据库用户名
    sonar.datasource.password=xxx       //sonar数据据密码
对应的配置类如下：

    public class QaConfig {
        @Value("${datasource.url}")
    String url;
    @Value("${datasource.username}")
        String username;
        @Value("${datasource.password}")
        String password;

       @Value("${sonar.datasource.url}")
    String sonarUrl;
   @Value("${sonar.datasource.username}")
       String sonarUserName;
       @Value("${sonar.datasource.password}")
       String sonarPassword;
    
     public ServerConfig serverConfigAdcloud_asDefault() {
        return getServerConfig("oracle.jdbc.OracleDriver", userName, password, url, "adcloud", true, 0, 30);
    }
        
    public ServerConfig serverConfigSonar() {
        return getServerConfig("oracle.jdbc.OracleDriver", sonarUserName, sonarPassword, sonarUrl, "sonar", false, 0, 30);
    }
    }

---------------------
开启 web application 


    mvn spring-boot:run "-Drun.jvmArguments=-Dserver.port=8081 -Dspring.profiles.active=qa"

    可选参数 -Dcontext-path=/adcloud

    或者
    
    mvn clean compile package
    java -jar target/adcloud-common-1.0-SNAPSHOT.jar --server.port=8099 --spring.profiles.active=qa

    可选参数 --context-path=/adcloud

    
###3.docker镜像
如果你想创建项目的dockor镜像，你可以参照以下命令：

    docker build -t adcloudbe .
    docker run -d --name adcloudbe -e TZ="Asia/Shanghai" -p 8090:8080 -t adcloudbe java -jar adcloud-common-1.0-SNAPSHOT.jar --spring.profiles.active=qa,docker

    docker build -t dcos/adcloudbe docker-with-volume
    docker run -d --name adcloudbe -e TZ="Asia/Shanghai" -p 20023:8080  -v yourtarget:/usr/app/target  --privileged=true -t dcos/adcloudbe java -jar target/adcloud-common-1.0-SNAPSHOT.jar --spring.profiles.active=qa,dtest
    
###4.依赖包下载
如果可以访问外网 则通过在pom.xml配置下载依赖包：

    <repositories>
        <repository>
            <id>public-adcloud</id>
            <name>PUBLIC-ADCLOUD</name>
            <url>http://10.70.181.10:8081/nexus/content/repositories/maven_public/</url>
        </repository>
        <repository>
            <id>adcloud</id>
            <name>ADCLOUD</name>
            <url>http://10.70.181.10:8081/nexus/content/repositories/public-adcloud/</url>
        </repository>
    </repositories>
    
如果不可以访问外网，可以在gitlab上下载项目所用的依赖包。

###5.免登录接口配置
如果你需要配置一些免登录接口，你可以在CommConstants类中配置。
例如安全扫描结果查看免登录配置：
          
    UN_CHECK_METHOD.add("/webscan/doScan"); //接口/webscan/doScan路径根据需要配置


###6.系统配置表

   ad_static_data : 系统静态数据表
   ad_para_detail : 参数配置表
 
 ad_static_data 存储项目中必须的数据，一些数据需要手动插入，以下是需要手动插入的数据：
 
 1.jenkins 回调地址配置： 用于返回jenkins job 执行状态
    
    参数
    ${BACKURL} = http://后台地址:port/adcloud/jobNotify
    
    sql语句
    insert into ad_static_data (CODE_TYPE, CODE_VALUE, CODE_NAME,  CODE_DESC, CODE_TYPE_ALIAS, SORT_ID, STATE, EXTERN_CODE_TYPE)
    values ('JENKINS_SET', 'BACKURL', ${BACKURL}, '回调地址', '', 1, 'U', '');

2.Artifactor仓库配置: 用于存储二进制包

    参数
    ${factory} = 搭建好的artifactory应用中的仓库名称。 如：adcloud_fe_test
  
    sql语句：
    insert into ad_static_data (CODE_TYPE, CODE_VALUE, CODE_NAME, 
    CODE_DESC,  CODE_TYPE_ALIAS, SORT_ID, STATE, EXTERN_CODE_TYPE)
    values ('ARTIFACTORY', ${factory}, '', 'Artifactory仓库名','', 1, 'U', '');

3.虚机发布计划中虚机发布中转主机配置

    参数
    ${VM_IP} = 1.1.1.1:user:Aidevops@2016 
      主机ip:主机用户名:主机密码
     
    sql语句
    insert into ad_static_data (CODE_TYPE, CODE_VALUE, CODE_NAME, CODE_DESC,CODE_TYPE_ALIAS,SORT_ID, STATE, EXTERN_CODE_TYPE) 
    values ('VM_DEPLOY_IP', '${VM_IP}', '虚机发布中转主机', '', '', null, '', '/app/aidevops/sbin');

4.sonar扫描结果配置:用于查看sonar扫描的结果
 
    参数 
    ${sonar_ip} = http://ip:port/overview?
   
    sql语句：
    insert into ad_static_data (CODE_TYPE, CODE_VALUE, CODE_NAME, 
    CODE_DESC,  CODE_TYPE_ALIAS, SORT_ID, STATE, EXTERN_CODE_TYPE)
    values ('SONAR_IP', ${sonar_ip}, '', 'sonar扫描结果ip','', 1, 'U', '');

5. ad_user里配置一条用户名为 system 的数据，用于流水线自动构建，默认显示系统管理员操作

        insert into ad_user(user_id,login_name,password,display_name,state) values(1,'system','','系统构建',1);

##五、系统管理员权限介绍
本系统有三大角色，系统管理员，项目管理员，项目成员。
各个角色权限如下图:
![本地址](http://img.blog.csdn.net/20170703171824268?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvenhzX3R5aA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

在数据库中配置系统管理员的涉及到下面两张表
ad_role 角色表
ad_user_role_rel 用户角色关系表
ad_role 默认会插入
在 ad_user_role_rel 配置用户名和 ad_role中角色的关系

    sql语句
    insert into ad_user_role_rel (REL_ID, USER_NAME, ROLE_ID, STATE, CREATE_DATE, OP_USER)
    values (1, '${userName}', '${adRoleId}', 1, sysdate, null);


-----------

###版权声明 

1. 您可以在完全遵守本最终用户授权协议的基础上，将本软件应用于非商业用途，而不必支付软件版权授权费用。 

2. 您可以在协议规定的约束和限制范围内修改 ADCloud持续交付云平台 源代码(如果被提供的话)或界面以适应您的网站要求。 

3. 您拥有使用本软件构建的管理系统全部资料及相关信息的所有权，并独立承担与资料和信息的相关法律义务。 




