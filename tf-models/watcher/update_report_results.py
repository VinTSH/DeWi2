import firebase_admin
from firebase_admin import credentials, firestore
import json
import os
import time
import shutil

# 初始化 Firebase
cred = credentials.Certificate("firebase-key.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

OUTPUT_FOLDER = "tf-watch/output"
PROCESSED_FOLDER = "tf-watch/processed"

def update_report_from_json(json_path):
    try:
        with open(json_path, 'r') as f:
            data = json.load(f)

        image_name = data.get("imageName")
        confidence = data.get("confidence")
        grade = data.get("grade")

        if not image_name or confidence is None or not grade:
            print(f"[⚠️] JSON 檔案格式錯誤：{json_path}")
            return

        # 根據 imageName 搜尋 Firestore 的報告
        reports_ref = db.collection("reports")
        query = reports_ref.where("imageName", "==", image_name).stream()
        updated = False

        for doc in query:
            doc_ref = reports_ref.document(doc.id)
            doc_ref.update({
                "status": "analyzed",
                "confidence": confidence,
                "grade": grade
            })
            print(f"[✅] Firestore 更新完成：{image_name}")
            updated = True

        if not updated:
            print(f"[❌] 找不到 imageName 對應的報告：{image_name}")

        # 移動 JSON 去 processed folder
        os.makedirs(PROCESSED_FOLDER, exist_ok=True)
        shutil.move(json_path, os.path.join(PROCESSED_FOLDER, os.path.basename(json_path)))

    except Exception as e:
        print(f"[❌] 錯誤處理 {json_path}：{e}")

def scan_and_update():
    for filename in os.listdir(OUTPUT_FOLDER):
        if filename.endswith(".json"):
            json_path = os.path.join(OUTPUT_FOLDER, filename)
            update_report_from_json(json_path)

if __name__ == "__main__":
    print("📡 正在掃描分析結果 JSON 檔並更新 Firestore...")
    while True:
        scan_and_update()
        time.sleep(5)
