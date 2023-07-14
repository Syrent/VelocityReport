![Header](https://capsule-render.vercel.app/api?type=Waving&color=timeGradient&height=200&animation=fadeIn&section=header&text=Velocity%20Report&fontSize=65)

# 👋 欢迎使用Velocity Report

<!-- Data Statistics / 数据统计 -->
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/Syrent/VelocityReport/maven.yml?color=%2300B4DB&style=flat-square&logo=github)](https://github.com/Syrent/VelocityReport/actions)
[![Bstats server stats](https://img.shields.io/bstats/servers/16576?color=%2300B4DB&style=flat-square&logo=serverless&logoColor=white)](https://bstats.org/plugin/bukkit/VelocityReport/16576)
[![Bstats player stats](https://img.shields.io/bstats/players/16576?color=%2300B4DB&style=flat-square&logo=odnoklassniki&logoColor=white)](https://bstats.org/plugin/bukkit/VelocityReport/16576)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/mqGjZEIE?label=Modrinth&color=blue&style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/velocityreport)
[![Spigot Downloads](https://img.shields.io/spiget/downloads/105378?label=Spigot&color=blue&style=flat-square&logo=docusign&logoColor=white)](https://www.spigotmc.org/resources/105378)
[![Polymart Downloads](https://img.shields.io/polymart/downloads/2896?label=Polymart&color=blue&style=flat-square&logo=docusign&logoColor=white)](https://polymart.org/resource/2896)

### [VelocityReport]是一款高性能且简单易用的举报插件，并提供了高度的可自定义性和MySQL支持等。

### 插件支持[Velocity]/[Spigot]/[Paper]/[Purpur]。

<table>
<thead>
<tr>
<th width="2000" colspan="2">
</th>
<h2>🧭 快捷链接</h2>
</tr>
</thead>
<tbody>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Syrent/VelocityReport/wiki"><img src="..//images/icon/bookmark.svg"></a>
  </td>
  <td valign="top">
    <h3>Wiki</h3>
    <p>
      不懂的如何使用？<a href="https://github.com/Syrent/VelocityReport/wiki">点此开始快速入门</a>！
    <br>
      <a href="https://github.com/Syrent/VelocityReport/wiki/Why-VelocityReport">以及为什么要选择Velocity Reoprt</a>？
    </p>
  </td>
</tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://discord.gg/VZk2XU3kFg"><img src="../images/icon/support.svg"></a>
  </td>
  <td>
    <h3>帮助支持</h3>
    <p>
      您可加入我们的<a href="https://discord.gg/VZk2XU3kFg">Discord社区</a>获得更多帮助。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Syrent/VelocityReport/issues"><img src="../images/icon/bug.svg"></a>
  </td>
  <td>
    <h3>BUG反馈</h3>
    <p>
      在使用插件时遇到了烦人的BUG？<a href="https://github.com/Syrent/VelocityReport/issues">请点击该链接与我们反馈</a>。
    </p>
  </td>
</tr>
</tbody>
</table>

<table>
<thead>
<tr>
<th width="2000" colspan="2">
</th>
<h2>🚀 插件发布页</h2>
</tr>
</thead>
<tbody>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://github.com/Syrent/VelocityReport/releases"><img src="../images/logo/github-mark.svg"></a>
  </td>
  <td valign="top">
    <h3>Github Releases</h3>
    <p>
      <a href="https://github.com/Syrent/VelocityReport/releases">点击前往Github发布页</a>。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://modrinth.com/plugin/velocityreport"><img src="../images/logo/modrinth.svg"></a>
  </td>
  <td valign="top">
    <h3>Modrinth</h3>
    <p>
      <a href="https://modrinth.com/plugin/velocityreport">点击前往SpigotMC插件发布页</a>。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://www.spigotmc.org/resources/91842"><img src="../images/logo/spigotmc.png"></a>
  </td>
  <td valign="top">
    <h3>SpigotMC</h3>
    <p>
      <a href="https://www.spigotmc.org/resources/91842">点击前往SpigotMC插件发布页</a>。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://polymart.org/resource/2896"><img src="../images/logo/polymart.png"></a>
  </td>
  <td valign="top">
    <h3>Polymart</h3>
    <p>
      <a href="https://polymart.org/resource/2896">点击前往Polymart插件发布页</a>。
    </p>
  </td>
</tr>
<tr>
  <td width="80" align="center" valign="top">
    <br>
    <a href="https://forums.papermc.io/threads/515"><img src="../images/logo/papermc-logo.png"></a>
  </td>
  <td valign="top">
    <h3>PaperMC</h3>
    <p>
      <a href="https://forums.papermc.io/threads/515">点击前往PaperMC论坛插件发布页</a>。
    </p>
  </td>
</tr>
</tbody>
</table>

# 🛠 编译插件
⚠ 编译需要使用JDK8以及更高版本

若要编译插件，请在终端内使用`./gradlew build`。 
编译完成后，可在`/bin`文件夹内获取到编译成品。   

# 🍪 帮助贡献
如果这个插件以任何方式帮助了你，并且你想贡献自己的一份力量来让插件变得更好，那么有很多方法帮助你：

* 打开一个**pull request**，包含一个新的功能/改动或错误修复，它们将很有效的帮助到许多用户。
同时发起**pulll request**时请保证其的详细度和高质量，一个bug在1周内得到修复与1小时内得到修复的区别就在于报告的质量（通常提供正确的重现步骤，而这些步骤实际上是有效的）。
* 通过反馈Wiki相关问题来帮助改进Wiki，您可以在其中改进现有的描述，添加您发现缺少的信息，修正错别字/语法错误或添加更多的函数使用示例。

## 🌐 语言切换

语言切换 / Need to switch languages?

[![English](https://img.shields.io/badge/English-Click%20me-purple?style=flat-square)](https://github.com/Syrent/VelocityReport/blob/master/README.md)
[![简体中文](https://img.shields.io/badge/简体中文-Click%20me-purple?style=flat-square)](https://github.com/Syrent/VelocityReport/blob/master/blob/zh-hans/README.md)


2023 © [Syrent](https://github.com/Syrent)

<!-- URL LIST -->
[Spigot]: https://www.spigotmc.org
[Paper]: https://papermc.io
[Purpur]: https://purpurmc.org
[Velocity]: https://velocitypowered.com
[VelocityReport]: https://github.com/Syrent/VelocityReport

<!-- Rev1.0 Designed by chencu5958 -->
<!-- SVG icons from svgrepo.com -->