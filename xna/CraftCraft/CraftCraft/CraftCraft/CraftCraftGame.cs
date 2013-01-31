using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Content;
using Microsoft.Xna.Framework.GamerServices;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;
using Microsoft.Xna.Framework.Media;
using CraftCraft.Engine;
using CraftCraft.Input.DirectInput;

namespace CraftCraft
{
    /// <summary>
    /// This is the main type for your game
    /// </summary>
    public class CraftCraftGame : Microsoft.Xna.Framework.Game
    {
        private static GraphicsDeviceManager graphics;
        private static GraphicsDevice graphicsDevice;
        private static ContentManager contentManager;

        private FirstPersonCamera camera;

        private VertexPositionColor[] verts;
        private VertexBuffer vertBuffer;
        private BasicEffect basicEffect;

        public static SpriteFont mainFont;
        public static SpriteBatch spriteBatch;
        private DirectInputGamepad gamePad;
        private Chunk chunk;
        private List<OctTreeNode> renderList = new List<OctTreeNode>();
        private VertexPositionTexture[] texVerts;


        public CraftCraftGame()
        {
            graphics = new GraphicsDeviceManager(this);
            Content.RootDirectory = "Content";
            contentManager = this.Content;
        }

        /// <summary>
        /// Allows the game to perform any initialization it needs to before starting to run.
        /// This is where it can query for any required services and load any non-graphic
        /// related content.  Calling base.Initialize will enumerate through any components
        /// and initialize them as well.
        /// </summary>
        protected override void Initialize()
        {
            graphicsDevice = graphics.GraphicsDevice;

            camera = new FirstPersonCamera(new Vector3(0, 0, -6), new Vector3(0, 0, 1), new Vector3(0, 1, 0));
            camera.Initialize(graphicsDevice);
            spriteBatch = new SpriteBatch(graphicsDevice);

            basicEffect = new BasicEffect(graphicsDevice);

            //List<DirectInputGamepad> gamePads = DirectInputGamepad.Gamepads;
            //gamePad = gamePads[0];


            base.Initialize();
            Console.WriteLine("Done with Init.");
        }

        /// <summary>
        /// LoadContent will be called once per game and is the place to load
        /// all of your content.
        /// </summary>
        protected override void LoadContent()
        {
            mainFont = Content.Load<SpriteFont>("MainFont");
            Chunk.loadTextures(this.Content);
            chunk = new Chunk();
            chunk.load("/z/random.chunk");

            verts = new VertexPositionColor[4];
            verts[0] = new VertexPositionColor(new Vector3(-3, -3, 0), Color.Red);
            verts[1] = new VertexPositionColor(new Vector3(3, -3, 0), Color.Green);
            verts[2] = new VertexPositionColor(new Vector3(-3, 3, 0), Color.Blue);
            verts[3] = new VertexPositionColor(new Vector3(3, 3, 0), Color.Yellow);
            //vertBuffer = new VertexBuffer(graphicsDevice, VertexPositionColor.VertexDeclaration, verts.Length, BufferUsage.None);
            //vertBuffer.SetData<VertexPositionColor>(verts);
            texVerts = new VertexPositionTexture[4];
            texVerts[0] = new VertexPositionTexture(new Vector3(-3, -3, 0), new Vector2(0, 0));
            texVerts[1] = new VertexPositionTexture(new Vector3(3, -3, 0), new Vector2(1, 0));
            texVerts[2] = new VertexPositionTexture(new Vector3(-3, 3, 0), new Vector2(0, 1));
            texVerts[3] = new VertexPositionTexture(new Vector3(3, 3, 0), new Vector2(1, 1));
            Console.WriteLine("Done loading Content");
        }

        public static GraphicsDevice GD
        {
            get { return graphicsDevice; }
        }

        public static ContentManager CM
        {
            get { return contentManager; }
        }

        /// <summary>
        /// UnloadContent will be called once per game and is the place to unload
        /// all content.
        /// </summary>
        protected override void UnloadContent()
        {
            // TODO: Unload any non ContentManager content here
        }

        /// <summary>
        /// Allows the game to run logic such as updating the world,
        /// checking for collisions, gathering input, and playing audio.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Update(GameTime gameTime)
        {

            //if (gamePad.Buttons.List[0] == ButtonState.Pressed) 
            //    Console.WriteLine("Button 0");

            // Allows the game to exit
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back == ButtonState.Pressed)
                this.Exit();

            GamePadState gamepadState = GamePad.GetState(PlayerIndex.One);

            if (gamepadState.Buttons.Back == ButtonState.Pressed)
                this.Exit();

            Vector3 translation = new Vector3(gamepadState.ThumbSticks.Left.X, 0, -gamepadState.ThumbSticks.Left.Y);
            camera.Update(translation, -gamepadState.ThumbSticks.Right.X, gamepadState.ThumbSticks.Right.Y);


            ProcessKeyboard();

            base.Update(gameTime);
        }

        private void ProcessKeyboard()
        {
            float STRAFE_STEP = .05f;
            float FORWARD_BACKWARD_STEP = .05f;
            float LEFTRIGHTROT_STEP = .3f;
            float UPDOWNROT_STEP = .3f;

            KeyboardState keybState = Keyboard.GetState();

            if (keybState.IsKeyDown(Keys.Escape))
                this.Exit();

            float x = 0, y = 0, z = 0;
            float leftRightRot = 0, upDownRot = 0;

            if (keybState.IsKeyDown(Keys.A))
                x += STRAFE_STEP;
            if (keybState.IsKeyDown(Keys.D))
                x -= STRAFE_STEP;

            if (keybState.IsKeyDown(Keys.W))
                z += FORWARD_BACKWARD_STEP;
            if (keybState.IsKeyDown(Keys.S))
                z -= FORWARD_BACKWARD_STEP;

            if (keybState.IsKeyDown(Keys.Left))
                leftRightRot += LEFTRIGHTROT_STEP;
            if (keybState.IsKeyDown(Keys.Right))
                leftRightRot -= LEFTRIGHTROT_STEP;

            if (keybState.IsKeyDown(Keys.Up))
                upDownRot -= UPDOWNROT_STEP;
            if (keybState.IsKeyDown(Keys.Down))
                upDownRot += UPDOWNROT_STEP;

            Vector3 translation = new Vector3(x, 0, z);
            camera.Update(translation, leftRightRot, upDownRot);
        }

        /// <summary>
        /// This is called when the game should draw itself.
        /// </summary>
        /// <param name="gameTime">Provides a snapshot of timing values.</param>
        protected override void Draw(GameTime gameTime)
        {
            graphicsDevice.RasterizerState = RasterizerState.CullNone;
            graphicsDevice.SamplerStates[0] = new SamplerState() { Filter = TextureFilter.Point };
            //basicEffect.VertexColorEnabled = true;
            basicEffect.TextureEnabled = true;
            basicEffect.LightingEnabled = false;


            graphicsDevice.Clear(Color.CornflowerBlue);

            basicEffect.World = Matrix.Identity;
            basicEffect.View = camera.viewMatrix;
            basicEffect.Projection = camera.projectionMatrix;

            Properties.resetRenderAndCullingCounters();

            renderList.Clear();
            chunk.buildRenderList(camera, renderList);
            basicEffect.Texture = Chunk.textures[(int)BlockType.GRASS];

            foreach (EffectPass pass in basicEffect.CurrentTechnique.Passes)
            {
                pass.Apply();

                OctTreeNode.renderNodes(basicEffect, camera, renderList);
                //graphicsDevice.DrawUserPrimitives<VertexPositionTexture>(PrimitiveType.TriangleStrip, texVerts, 0, 2);
            }

            spriteBatch.Begin();
            DrawHud();
            DrawText();
            spriteBatch.End();
            base.Draw(gameTime);
        }

        private void DrawHud()
        {
        }

        private void DrawText()
        {
            spriteBatch.DrawString(mainFont, "Pos: " + camera.position + ", Angle:" + camera.leftRightRot + "," + camera.upDownRot, new Vector2(10, 10), Color.White);
            spriteBatch.DrawString(mainFont, "Target: " + camera.target + ", Up:" + camera.upVector + "," + camera.upDownRot, new Vector2(10, 50), Color.White);
            spriteBatch.DrawString(mainFont, Properties.getRenderAndCullingString(), new Vector2(10, graphicsDevice.Viewport.Height - 30), Color.White);
        }
    }
}
