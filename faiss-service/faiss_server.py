from fastapi import FastAPI
from pydantic import BaseModel
import faiss
import numpy as np

#venv\Scripts\activate
#uvicorn faiss_server:app --port 8001
app = FastAPI()

dim = 768
index = faiss.IndexFlatL2(dim)
id_map = []

# ✅ 定义请求体
class AddRequest(BaseModel):
    vectors: list
    ids: list

class SearchRequest(BaseModel):
    vector: list
    top_k: int = 5

@app.post("/add")
def add_vector(req: AddRequest):
    global index, id_map

    vec = np.array(req.vectors).astype("float32")
    index.add(vec)

    id_map.extend(req.ids)

    return {"status": "ok", "count": len(id_map)}


@app.post("/search")
def search_vector(req: SearchRequest):
    vec = np.array([req.vector]).astype("float32")

    D, I = index.search(vec, req.top_k)

    result_ids = []
    for i in I[0]:
        if i < len(id_map):
            result_ids.append(id_map[i])

    return {
        "ids": result_ids,
        "distances": D[0].tolist()
    }