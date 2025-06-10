import firebase_admin
from firebase_admin import credentials, firestore
import requests
import os
import time

# 初始化 Firebase
cred = credentials.Certificate("firebase-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

WATCH_FOLDER = "tf-watch/input"  # 你同學個 folder，你哋可以講好路徑

def download_image(image_url, filename):
    try:
        os.makedirs(WATCH_FOLDER, exist_ok=True)
        response = requests.get(image_url)
        if response.status_code == 200:
            full_path = os.path.join(WATCH_FOLDER, filename)
            with open(full_path, "wb") as f:
                f.write(response.content)
            print(f"[✅] 圖片已搬到分析資料夾：{filename}")
            return True
        else:
            print(f"[❌] 下載失敗：{image_url}")
            return False
    except Exception as e:
        print(f"[錯誤] {e}")
        return False

def move_pending_images():
    reports_ref = db.collection("reports")
    docs = reports_ref.where("status", "==", "pending").stream()

    for doc in docs:
        data = doc.to_dict()
        image_url = data.get("imageUrl")
        image_name = data.get("imageName", f"{doc.id}.jpg")

        dest_path = os.path.join(WATCH_FOLDER, image_name)
        if os.path.exists(dest_path):
            continue  # 已處理過，略過

        if download_image(image_url, image_name):
            print(f"[📥] 圖片搬好：{image_name}")
        else:
            print(f"[⚠️] 略過失敗圖片：{doc.id}")

if __name__ == "__main__":
    print("📡 正在搬運 pending 圖片到 AI 分析資料夾...")
    while True:
        move_pending_images()
        time.sleep(5)
