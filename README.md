# 睿思报表
睿思报表使用B/S架构、纯java编写，服务端程序可以在Window、Linux、Unix等多个系统上运行，客户端无需安装，用户只需使用IE、firefox等浏览器就能设计、浏览、打印报表。功能强大、灵活，使用简单。

# 产品安装

## 安装数据
1.	数据是bi_report.bak文件，在doc目录中，对应MYSQL数据库。  <br/>
2.	进入MYSQL建立 bi_report 数据库，注意名称一致。 <br/>
3.	用mysql命令还原数据到数据库中，相关命令为：mysql –uroot –pxxxxxx bi_report <bi_report.bak; <br/>

## 安装程序
1.	直接把ruisi-report.war拷入 TOMCAT 的 webapps 目录，目录名称 report。
2.	修改 ruisi-report/WEB-INF/classes/database.properties 文件，主要修改 password 这项内容，既你数据库账户root的密码，如果您的MYSQL root密码是123456，则不用修改。
3.	启动tomcat, 输入 http://localhost:8080/ruisi-report 访问系统，如果看到登录页面，系统配置成功。
4.	登录用户名/密码：admin/123456

# 产品截图
在线报表设计器<br/>
![olap](http://www.ruisitech.com/img/report1.png?v2)  <br/>
报表预览<br/>
![1](http://www.ruisitech.com/img/report2.png?v2)  <br/>
报表授权<br/>
![2](http://www.ruisitech.com/img/report3.png?v4)  <br/>