using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace CraftCraft.Engine
{
    #region BaseCamera
    public class Camera
    {
            public Vector3 position;
            public Vector3 target;
            public Vector3 upVector;
            public Vector3 lockedTarget;

            public Matrix viewMatrix;
            public Matrix projectionMatrix;
            public float fieldOfView = MathHelper.PiOver4;
            public float nearPlaneDistance = 0.1f;
            public float farPlaneDistance = 500f;

            public BoundingFrustum frustrum;

            public bool frustrumLocked = false;

            public Camera(Vector3 position, Vector3 target, Vector3 upVector)
            {
                this.position = position;
                this.target = target;
                this.upVector = upVector;
                frustrum = new BoundingFrustum(new Matrix());
            }

            public void Initialize(GraphicsDevice graphicsDevice)
            {
                UpdateViewMatrix();
                float aspectRatio = graphicsDevice.Viewport.AspectRatio;
                projectionMatrix = Matrix.CreatePerspectiveFieldOfView(fieldOfView, aspectRatio, nearPlaneDistance, farPlaneDistance);
            }

            protected void UpdateViewMatrix()
            {
                viewMatrix = Matrix.CreateLookAt(position, target, upVector);
                if (!frustrumLocked)
                {
                    frustrum.Matrix = Matrix.Multiply(viewMatrix, projectionMatrix);
                }
            }            
    }
    #endregion

    #region FPSCamera
    public class FirstPersonCamera : Camera
    {
        public Vector3 cameraReference;

        public float leftRightRot;
        public float upDownRot;

        public float rotationSpeed = 0.05f;
        public float translationSpeed = 1f;

        public FirstPersonCamera(Vector3 position, Vector3 target, Vector3 upVector)
            : base(position, target, upVector)
        {
            cameraReference = target;
        }

        public void Update(Vector3 translation, float leftRightRot, float upDownRot)
        {
            // wrap the left-right rotation
            this.leftRightRot += leftRightRot * rotationSpeed;
            if (this.leftRightRot >= MathHelper.TwoPi || this.leftRightRot <= -MathHelper.TwoPi)
            {
                this.leftRightRot = 0f;
            }

            // clamp up-down rotation
            this.upDownRot += upDownRot * rotationSpeed;
            if (this.upDownRot >= MathHelper.PiOver2)
            {
                this.upDownRot = MathHelper.PiOver2;
            }
            else if (this.upDownRot <= -MathHelper.PiOver2)
            {
                this.upDownRot = -MathHelper.PiOver2;
            }

            
            Matrix leftRightRotMatrix = Matrix.CreateRotationY(this.leftRightRot);
            Matrix rotationMatrix = Matrix.CreateRotationX(this.upDownRot) * leftRightRotMatrix;
            Vector3 transformedReference = Vector3.Transform(cameraReference, rotationMatrix);

            // only translate base on rotations about the Y-axis, otherwise we'd move vertically if we allowed rotation
            // in the X or Z axis.
            position += Vector3.Transform(translation, leftRightRotMatrix) * translationSpeed;
            target = transformedReference + position;

            UpdateViewMatrix();
        }
    }
    #endregion
}
