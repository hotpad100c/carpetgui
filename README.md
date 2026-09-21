# CarpetGUI

CarpetGUI 是一个为 [Fabric Carpet](https://github.com/gnembon/fabric-carpet) 模组设计的图形化配置界面（GUI），让玩家能够直观、便捷地查看、搜索和调整所有的 Carpet 规则与规则预设。

## 特性 (Features)

- **可视化配置**：通过 GUI 界面轻松浏览与搜索所有 Carpet 规则，支持分类、快速切换和自定义输入。
- **规则集管理 (Rule Stack & Prefab)**：支持保存、应用和管理多组规则预设，方便在不同场景下快速切换。
- **跨版本支持**：基于 Stonecutter 构建，适配从 Minecraft 1.16.5 到 26.3 (及更高版本)。

## 依赖 (Dependencies)

- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Carpet](https://modrinth.com/mod/carpet)

> 注：2.0.0开始，本模组所有版本均**不再需要**安装 owo-lib。

## 构建与发布 (Building & Publishing)

### 本地构建
```bash
./gradlew build
```


## 开源许可与致谢 (License & Acknowledgements)

CarpetGUI 采用 [MIT 许可证](LICENSE) 开源。

### owo-lib 致谢 (Third-Party Notice)
本模组内置的 UI 组件体系在设计与实现上参考及移植了 [owo-lib](https://github.com/wisp-forest/owo-lib) 的架构思路。owo-lib 遵循 MIT 开源许可证：

```text
MIT License

Copyright (c) 2021-2024 glisco03, Wisp Forest

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
