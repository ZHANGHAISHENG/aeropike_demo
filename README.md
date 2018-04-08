
对比redis:
     优点：集群管理更加强大
Redis需要开发人员自己管理分片并提供分片算法用于在各分片之间平衡数据；而AerospikeDB可以自动处理相当于分片的工作；
在Redis中，为了增加吞吐量，需要增加Redis分片的数量，并重构分片算法及重新平衡数据，这通常需要停机；而在AerospikeDB中，可以动态增加数据卷和吞吐量，无需停机，并且AerospikeDB可以自动平衡数据和流量；
在Redis中，如果需要复制及故障转移功能，则需要开发人员自己在应用程序层同步数据；而在AerospikeDB中，只需设置复制因子，然后由AerospikeDB完成同步复制操作，保持即时一致性；而且AerospikeDB可以透明地完成故障转移；
此外，AerospikeDB既可以完全在内存中运行，也可以利用Flash/SSD存储的优点。
       缺点：数据操作模型没有redis多、社区版无法创建用户

广告行业应用广泛
参考：
https://www.cnblogs.com/xiaoit/p/4552645.html
性能比较：http://www.infoq.com/cn/news/2015/02/aerospikedb-redis-aws-nosql
业务场景对比：https://blog.csdn.net/hotallen/article/details/55060796

入门安装：
https://www.aerospike.com/docs/operations/install/vagrant/win
https://www.cnblogs.com/xiaoit/p/4548634.html

namespace 配置：
https://blog.csdn.net/u011344514/article/details/53082757

官网文档：
https://www.aerospike.com/docs/



安装提示没有公钥解决：
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 6AF0E1940624A220 #此处6AF0E1940624A220需要是错误提示的key  


启动、关闭、查看状态：
service aerospike start 
service aerospike stop
service aerospike status

amc控制台：
#启动amc
sudo /etc/init.d/amc start
#停止amc
sudo /etc/init.d/amc stop
#重启amc
sudo /etc/init.d/amc restart
#查看amc状态
sudo /etc/init.d/amc status
#查看amc启动报错日志
/var/log/amc/error.log

访问： http://ip:8081
配置位置：/etc/amc/amc.conf


进入sql控制台：aql
进入管理员控制台：asadm


集群环境搭建:
 官方推荐模式 multicast 多播模式：
只需在 每台服务器aerospike.conf heartbeat中添加： address 192.168.0.105（本机ip）既可
heartbeat {
		mode multicast
		multicast-group 239.1.99.222
		port 9918
                address 192.168.0.105
		# To use unicast-mesh heartbeats, remove the 3 lines above, and see
		# aerospike_mesh.conf for alternative.
		interval 150
		timeout 10
	}

修改配置astools.conf：
-- 不修改时，只要本机启动也能连接集群
[cluster]
host = "localhost:cluster_a:3000,192.168.0.107:3000" 

环境搭建示例：
硬盘下：Ubuntu16.04、Ubuntu16.04-2

用户密码设置：
对于Aerospike Enterprise Edition 3.5.14及更高版本，要创建用户，您必须安装blowfish文件加密（使用以下脚本）。一旦tarball被提取，install_bcrypt脚本就在安全目录中。
社区版本可能无法设置账号密码















