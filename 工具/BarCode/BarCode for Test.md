<center><font size=5 face="黑体">Batch Generate BarCode for Test</font></center>

日常开发测试中经常需要为测试数据<font color=geen face="黑体">**批量生成条码**</font>，方便进行PDA设备的测试验证。但一些在线生成条码的网站对于批量生成的需要支持都不是很好或者甚至不支持。转换一下思路，可以用条码字体来低成本的解决这个问题。

1、在 [Google Font](https://fonts.google.com/?preview.text=18860053&preview.text_type=custom&query=barcode) 上搜索一下，发现有BarCode 39和BarCode 128的字体，为了方便，我们选择 BarCode 39带文字显示的版本

字体文件：

- LibreBarcode39Text-Regular.ttf
- LibreBarcode39ExtendedText-Regular.ttf

2、字体文件下载下来之后，直接右键 → Install就可以了

3、把需要批量生成条码的数据复制到Excel中，在数据内容的开头和末尾添加一个星号( ***** )

4、把调整过后的数据字体调整为刚才导入的条码字体

