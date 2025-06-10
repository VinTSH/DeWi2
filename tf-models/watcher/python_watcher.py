import os
import time
import firebase_admin
from firebase_admin import credentials, firestore
import random
import urllib.request

# 初始化 Firebase Admin SDK
cred = credentials.Certificate("firebase-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# 圖片資料夾路徑
IMAGE_DIR = "tf-models/watcher/images"

# 確保圖片資料夾存在
if not os.path.exists(IMAGE_DIR):
    os.makedirs(IMAGE_DIR)

def download_image(image_url, local_path):
    urllib.request.urlretrieve(image_url, local_path)

def mock_analyze_image(image_path):
    # 模擬分析結果
    confidence = round(random.uniform(0.85, 0.99), 2)
    grade = random.choice(["A", "B", "C"])
    return confidence, grade

def process_report(doc_id, report_data):
    image_url = report_data.get("imageUrl")
    image_name = report_data.get("imageName")
    local_path = os.path.join(IMAGE_DIR, image_name)

    if not image_url:
        print(f"[SKIP] 缺少 imageUrl，報告 {doc_id} 略過")
        return

    # 下載 Firebase Storage 上嘅圖片
    try:
        download_image(image_url, local_path)
        print(f"[下載成功] {image_url} ➜ {local_path}")
    except Exception as e:
        print(f"[錯誤] 圖片下載失敗: {e}")
        return

    # 模擬分析
    print(f"[分析中] {local_path}")
    confidence, grade = mock_analyze_image(local_path)

    # 更新 Firestore
    db.collection("reports").document(doc_id).update({
        "confidence": confidence,
        "grade": grade,
        "status": "analyzed"
    })

    print(f"[完成] 報告已更新: {doc_id}，信心值={confidence}，評級={grade}")

    # 刪除本地圖片
    os.remove(local_path)

def watch_reports():
    while True:
        print("🔍 正在掃描 Firestore...")
        docs = db.collection("reports").where("status", "==", "pending").stream()

        for doc in docs:
            report = doc.to_dict()
            process_report(doc.id, report)

        time.sleep(10)  # 每 10 秒掃描一次

if __name__ == "__main__":
    print("🚀 Firebase Watcher 啟動中...")
    watch_reports()
