jfinal-sqlinxml
============

jfinal  sqlinxml plugin，查看其他插件-> [Maven](http://search.maven.org/#search%7Cga%7C1%7Ccn.dreampie)

maven 引用  ${jfinal-sqlinxml.version}替换为相应的版本如:0.1

```xml
<dependency>
 <groupId>cn.dreampie</groupId>
 <artifactId>jfinal-sqlinxml</artifactId>
 <version>${jfinal-sqlinxml.version}</version>
</dependency>
```


启用插件

```java

//sql语句plugin
plugins.add(new SqlInXmlPlugin());

```

sql文件以 xx_sql.xml结尾

```xml

<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<sqlRoot>

  <sqlGroup name="user">
    <sql id="findInfoBySelect">SELECT `user`.*,`userInfo`.street street,`userInfo`.gender gender,
      `userInfo`.zip_code zip_code,`province`.name province,`city`.name city,`county`.name
      county,`userRole`.role_id role_id
    </sql>
    <sql id="findInfoByFrom">FROM sec_user `user` LEFT JOIN sec_user_info `userInfo`
      ON(`user`.id=`userInfo`.id)
      LEFT JOIN sec_user_role `userRole` ON(`user`.id=`userRole`.user_id)
      LEFT JOIN com_area `province` ON(`userInfo`.province_id=`province`.id) LEFT JOIN com_area `city`
      ON(`userInfo`.city_id=`city`.id) LEFT JOIN com_area `county`
      ON(`userInfo`.county_id=`county`.id) LEFT JOIN sec_user_role `userRole` ON
      (`user`.id=`userRole`.user_id)
    </sql>
  </sqlGroup>

  <sqlGroup name="role">
    <sql id="findUserByFrom">FROM sec_role `role` WHERE `role`.id IN (SELECT `userRole`.role_id FROM
      sec_user_role `userRole` WHERE `userRole`.user_id=?)
    </sql>
  </sqlGroup>

  <sqlGroup name="userRole">

  </sqlGroup>

  <sqlGroup name="permission">
    <sql id="findRoleByFrom">FROM sec_permission `permission` WHERE `permission`.id IN (SELECT
      `role_permission`.permission_id FROM sec_role_permission
      `role_permission` WHERE `role_permission`.role_id=?)
    </sql>
  </sqlGroup>

  <sqlGroup name="rolePermission">

  </sqlGroup>
</sqlRoot>

```

获取sql内容

```java
//group+id
SqlKit.sql("user.findInfoBySelect")

```
