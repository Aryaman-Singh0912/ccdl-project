// Small helper that talks to the backend through /api and attaches the login token.
export async function api(path, { method = "GET", body, token } = {}) {
  const res = await fetch("/api" + path, {
    method,
    headers: {
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: "Bearer " + token } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 401 && token) {
    const err = new Error("Session expired, please log in again");
    err.status = 401;
    throw err;
  }
  if (!res.ok) {
    let message = "Request failed (" + res.status + ")";
    try {
      const data = await res.json();
      if (data.message) message = data.message;
    } catch (e) {
      /* response had no JSON body */
    }
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  return res.status === 204 ? null : res.json();
}
