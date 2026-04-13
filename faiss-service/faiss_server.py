from fastapi import FastAPI
from pydantic import BaseModel
import faiss
import numpy as np

# 启动：
# cd /d D:\study\Projects\ai-helper\faiss-service
# cd /d D:\WWW\CCC\faiss-service
# venv\Scripts\activate
# uvicorn faiss_server:app --port 8001

app = FastAPI()

# ====== 初始化（关键：使用 IndexIDMap 支持删除） ======
dim = 768
index = faiss.IndexIDMap(faiss.IndexFlatL2(dim))


# ====== 请求体 ======
class AddRequest(BaseModel):
    vectors: list
    ids: list


class SearchRequest(BaseModel):
    vector: list
    top_k: int = 5


class RemoveRequest(BaseModel):
    ids: list


# ====== 新增向量 ======
@app.post("/add")
def add_vector(req: AddRequest):
    vec = np.array(req.vectors).astype("float32")
    ids = np.array(req.ids).astype("int64")

    index.add_with_ids(vec, ids)

    return {"status": "ok"}


# ====== 向量检索 ======
@app.post("/search")
def search_vector(req: SearchRequest):
    vec = np.array([req.vector]).astype("float32")

    D, I = index.search(vec, req.top_k)

    result_ids = [int(i) for i in I[0] if i != -1]

    return {
        "ids": result_ids,
        "distances": D[0].tolist()
    }


# ====== 删除向量（关键能力） ======
@app.post("/remove")
def remove_vector(req: RemoveRequest):
    ids = np.array(req.ids).astype("int64")

    index.remove_ids(ids)

    return {"status": "ok"}