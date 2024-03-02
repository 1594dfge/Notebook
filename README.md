# SunnyWeather
SunnyWeather 是以 MVVM 架構去設計的天氣預報App，是使用中央氣象署-氣象資料開放平臺提供的API擷取需要的資料，再將資料進行處理，讓使用者能夠即時地查看當地地區的天氣狀況。

# 架構設計
<div>
SunnyWeather程序架構主要分為3層:UI控制層、ViewModel層、Repository層<br><br><br>
<img src="https://i.imgur.com/JACyZq6.gif"><br><br><br>
UI控制層:包含了Activity、Fragment、布局文件等與介面相關的東西<br>
ViewModel層:用於持有和UI元件相關的數據，以保證這些數據在屏幕旋轉時不會丟失，並且還要提供接口給UI控制層調用以及和Repository層進行通信<br>
Repository層:使用Retrofit訪問中央氣象署-氣象資料開放平臺服務器提供的WebAPI接口來擷取數據<br><br><br>
</div>

# 操作說明
<div>
申請授權碼<br>
如何申請授權碼請參考 網址:https://opendata.cwa.gov.tw/devManual/insrtuction<br><br><br>
</div>

<div>
1.請輸入要搜尋的縣市 例如:宜蘭縣<br>
全臺縣市鄉鎮對照圖 網址:https://opendata.cwa.gov.tw/opendatadoc/Opendata_City.pdf<br>
<img src="https://i.imgur.com/UGFbu4S.jpg" width="150" height="300"><br><br><br>
</div>

<div>
2.請選取要搜尋的鄉鎮 例如:三星鄉<br>
<img src="https://i.imgur.com/BZYsiLd.jpg" width="150" height="300"><br><br><br>
</div>

<div>
3.搜尋結果<br>
<img src="https://i.imgur.com/tRjPvjD.jpg" width="150" height="300">
<img src="https://i.imgur.com/PwV6uTf.jpg" width="150" height="300"><br><br><br>
</div>

<div>
4.重新搜尋 例如:宜蘭縣羅東鎮<br>
屏幕往左滑或者點選屏幕左上角房子圖示，就能顯示搜尋介面<br>
<img src="https://i.imgur.com/5q6nMNT.jpg" width="150" height="300">
<img src="https://i.imgur.com/575wbgU.jpg" width="150" height="300">
<img src="https://i.imgur.com/C0USTFj.jpg" width="150" height="300"><br><br><br>
</div>

<div>
5.更新天氣資訊<br>
屏幕往下滑，就能擷取最新天氣資訊<br>
<img src="https://i.imgur.com/qqqEjQF.jpg" width="150" height="300"><br><br><br>
</div>