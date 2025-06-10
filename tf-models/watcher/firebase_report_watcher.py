import firebase_admin
from firebase_admin import credentials, firestore
import time
import requests
import os
from predict_model import analyze_image  # 你自己寫好的 AI 函數

# ✅ 初始化 Firebase Admin SDK
cred = credentials.Certificate("firebase-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# ✅ 圖片下載
def download_image(image_url, local_path):
    response = requests.get(image_url)
    if response.status_code == 200:
        with open(local_path, 'wb') as f:
            f.write(response.content)
        print(f"[✅] 圖片下載完成：{local_path}")
    else:
        raise Exception(f"圖片下載失敗，HTTP {response.status_code}")

# ✅ 掃描 Firestore `reports`，分析 pending 圖片
def process_pending_reports():
    reports_ref = db.collection("reports")
    docs = reports_ref.where("status", "==", "pending").stream()

    for doc in docs:
        data = doc.to_dict()
        image_url = data.get("imageUrl")
        image_name = data.get("imageName", f"{doc.id}.jpg")
        if not image_url:
            print(f"[⚠️] 無 imageUrl，略過 {doc.id}")
            continue

        local_path = f"temp/{image_name}"
        os.makedirs("temp", exist_ok=True)

        try:
            download_image(image_url, local_path)
            confidence, grade = analyze_image(local_path)

            # ✅ 更新 Firestore 報告內容
            reports_ref.document(doc.id).update({
                "status": "analyzed",
                "confidence": confidence,
                "grade": grade
            })

            print(f"[🎯] 完成分析 {doc.id} ➜ Grade={grade}, Confidence={confidence}")
        except Exception as e:
            print(f"[❌] 錯誤處理 {doc.id}：{e}")

# ✅ 主流程輪詢（每 5 秒）
if __name__ == "__main__":
    print("🚀 Firestore Watcher 正在運行...")
    while True:
        process_pending_reports()
        time.sleep(5)
